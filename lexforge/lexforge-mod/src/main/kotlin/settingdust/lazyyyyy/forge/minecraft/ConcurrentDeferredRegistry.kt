package settingdust.lazyyyyy.forge.minecraft

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.registries.RegistryObject
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.map
import settingdust.lazyyyyy.merge
import settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry.RegistryObjectAccessor
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object ConcurrentDeferredRegistry {
    val entriesRegistering = hashSetOf<RegistryObject<*>>()
    val entryToRegistrationJob = ConcurrentHashMap<RegistryObject<*>, Job>()

    fun RegistryObject<*>.join() {
        if (entryToRegistrationJob.containsKey(this)) {
            runBlocking {
                entryToRegistrationJob[this@join]?.join()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> DeferredRegister<T>.concurrentAddEntries(
        event: RegisterEvent,
        entries: Map<RegistryObject<T>, Supplier<out T>>
    ) {
        entriesRegistering += entries.keys
        runBlocking(Dispatchers.IO + CoroutineName("Concurrent Deferred Registry")) {
            entries.asSequence().asFlow().concurrent()
                .map { (registryObject, supplier) ->
                    val deferred = async(start = CoroutineStart.LAZY) { supplier.get() }
                    val job = launch(start = CoroutineStart.LAZY) {
                        event.register(registryKey, registryObject.id) { runBlocking { deferred.await() } }
                        (registryObject as RegistryObjectAccessor).invokeUpdateReference(event)
                    }
                    entryToRegistrationJob[registryObject] = job
                    registryObject to job
                }
                .merge(false)
                .collect { (registryObject, job) ->
                    job.join()
                    entriesRegistering -= registryObject
                }
        }
        require(entriesRegistering.isEmpty()) {
            "Entries ${entriesRegistering.map { it.id }} failed to register"
        }
    }
}