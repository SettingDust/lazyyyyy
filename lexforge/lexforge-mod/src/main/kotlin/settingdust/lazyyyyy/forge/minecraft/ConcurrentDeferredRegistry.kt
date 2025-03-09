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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.registries.RegistryObject
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.collect
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.forge.LazyyyyyForge
import settingdust.lazyyyyy.map
import settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry.RegistryObjectAccessor
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object ConcurrentDeferredRegistry {
    val entriesRegistering = hashSetOf<RegistryObject<*>>()
    val entryToRegistrationJob = ConcurrentHashMap<RegistryObject<*>, Job>()

    fun RegistryObject<*>.join() {
        entryToRegistrationJob[this@join]?.let { runBlocking(Dispatchers.IO) { it.join() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> DeferredRegister<T>.concurrentAddEntries(
        event: RegisterEvent,
        entries: Map<RegistryObject<T>, Supplier<out T>>
    ) {
        val mutex = Mutex()
        entriesRegistering += entries.keys
        runBlocking(Dispatchers.IO + CoroutineName("Concurrent Deferred Registry #${registryName}")) {
            entries.asSequence().asFlow().concurrent()
                .map { (registryObject, supplier) ->
                    val deferred = async(start = CoroutineStart.LAZY) {
                        // Some mod use SPI, which depends on the classloader
                        Thread.currentThread().contextClassLoader = LazyyyyyForge::class.java.classLoader
                        supplier.get()
                    }
                    val job = launch(start = CoroutineStart.LAZY) {
                        val value = deferred.await()
                        mutex.withLock { event.register(registryKey, registryObject.id) { value } }
                        (registryObject as RegistryObjectAccessor).invokeUpdateReference(event)
                    }
                    entryToRegistrationJob[registryObject] = job
                    deferred.start()
                    job.invokeOnCompletion {
                        entryToRegistrationJob -= registryObject
                        entriesRegistering -= registryObject
                    }
                    job
                }
                .collect { it.join() }
        }
        if (entriesRegistering.isNotEmpty()) {
            Lazyyyyy.logger.error("Entries ${entriesRegistering.map { it.id }} failed to register")
        }
        entriesRegistering.clear()
    }
}