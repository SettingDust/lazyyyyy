package settingdust.lazyyyyy.forge.minecraft

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.registries.RegistryObject
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.mapNotNull
import settingdust.lazyyyyy.merge
import settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry.RegistryObjectAccessor
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

fun <T> DeferredRegister<T>.concurrentAddEntries(
    event: RegisterEvent,
    entries: Map<RegistryObject<T>, Supplier<out T>>
) = runBlocking(Dispatchers.IO) {
    val entriesInConcurrent = ConcurrentHashMap<RegistryObject<T>, Supplier<out T>>(entries)
    val entryToException = ConcurrentHashMap<RegistryObject<T>, Throwable>()
    var failedDepth = 0
    while (entriesInConcurrent.isNotEmpty() && failedDepth <= 20) {
        entriesInConcurrent.asSequence().asFlow().concurrent()
            .mapNotNull { (registryObject, supplier) ->
                try {
                    val value = supplier.get()
                    entriesInConcurrent -= registryObject
                    entryToException -= registryObject
                    registryObject to value
                } catch (e: Throwable) {
                    entriesInConcurrent[registryObject] = supplier
                    entryToException[registryObject] = e
                    null
                }
            }
            .merge(false)
            .collect { (registryObject, value) ->
                event.register(registryKey, registryObject.id) { value }
                (registryObject as RegistryObjectAccessor).invokeUpdateReference(event)
            }
        if (entriesInConcurrent.isNotEmpty()) failedDepth++
    }
    if (failedDepth > 20) {
        throw Exception("Failed to register for $entriesInConcurrent").apply {
            entryToException.forEach { (registryObject, exception) ->
                addSuppressed(Exception("Failed to register ${registryObject.id}", exception))
            }
        }
    }
}