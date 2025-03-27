package settingdust.lazyyyyy.forge.minecraft

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.registries.RegistryObject
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.forge.LazyyyyyForge
import settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry.RegistryObjectAccessor
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object ConcurrentDeferredRegistry {
    val entryToJob = Reference2ReferenceOpenHashMap<RegistryObject<*>, CompletableJob>()

    fun RegistryObject<*>.join() {
        if (entryToJob[this@join] != null) runBlocking { entryToJob[this@join]?.join() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> DeferredRegister<T>.concurrentAddEntries(
        event: RegisterEvent,
        entries: Map<RegistryObject<T>, Supplier<out T>>
    ) {
        val mutex = Mutex()
        val entryRegistering = ConcurrentHashMap.newKeySet<RegistryObject<T>>().apply { addAll(entries.keys) }
        for (registryObject in entries.keys) {
            entryToJob[registryObject] = Job()
        }
        runBlocking(Dispatchers.IO + CoroutineName("Concurrent Deferred Registry #${registryName}")) {
            entries.asSequence().asFlow().concurrent()
                .collect { (registryObject, supplier) ->
                    coroutineScope {
                        // Some mod use SPI, which depends on the classloader. Use the transforming layer here.
                        Thread.currentThread().contextClassLoader = LazyyyyyForge::class.java.classLoader
                        // FIXME has side effect. Like [ArmorItem]
                        val value = supplier.get()
                        mutex.withLock { event.register(registryKey, registryObject.id) { value } }
                        (registryObject as RegistryObjectAccessor).invokeUpdateReference(event)
                    }
                    entryRegistering -= registryObject
                    entryToJob[registryObject]!!.complete()
                }
        }
        if (entryRegistering.isNotEmpty()) {
            Lazyyyyy.logger.error("Entries ${entryRegistering.map { it.id }} failed to register")
        }
        entryRegistering.clear()
        for (registryObject in entries.keys) {
            if (entryToJob[registryObject]!!.complete()) {
                Lazyyyyy.logger.error("Entry ${registryObject.id} failed to complete")
            }
            entryToJob.remove(registryObject)
        }
    }
}