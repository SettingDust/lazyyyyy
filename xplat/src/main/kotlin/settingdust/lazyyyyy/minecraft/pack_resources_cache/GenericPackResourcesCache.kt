package settingdust.lazyyyyy.minecraft.pack_resources_cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy.PackRoot
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class GenericPackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    var namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init {
        try {
            scope.launch { loadCache() }
        } catch (e: Exception) {
            logger.error("Error loading pack cache in $pack", e)
        }
    }

    private suspend fun CoroutineScope.consumeRoot(
        root: Path,
        namespaces: ConcurrentHashMap<PackType, MutableSet<String>>
    ) {
        val strategy = PackRoot(root, null)
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#root/$root] caching") }
        root.listDirectoryEntries().asFlow().concurrent().collect { path ->
            val relativePath = root.relativize(path)
            if (path.isDirectory()) {
                val firstPath = relativePath.firstOrNull()
                val packType = packTypeByDirectory[firstPath?.name] ?: return@collect
                consumePackType(packType, path, PackRoot(root, path), namespaces)
            } else {
                consumeFile(this, path, strategy)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#root/$root] cached") }
    }

    private suspend fun CoroutineScope.consumePackType(
        type: PackType,
        directory: Path,
        strategy: CachingStrategy,
        namespaces: MutableMap<PackType, MutableSet<String>>
    ) {
        namespaces.computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] caching") }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] caching") }
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, Deferred<String>>>()
        directory.listDirectoryEntries().asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                namespaces[type]!! += path.name
                consumeResourceDirectory(path, directoryToFiles, strategy)
            } else {
                consumeFile(this, path, strategy)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/directoryToFiles] caching") }
        for ((path, files) in directoryToFiles) {
            this@GenericPackResourcesCache.directoryToFiles[path]!!.complete(files.mapValues { it.value.await() })
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/directoryToFiles] cached") }

        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] cached") }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] cached") }
    }

    private suspend fun CoroutineScope.cachePack() {
        for (type in PackType.entries) {
            namespaces.computeIfAbsent(type) { CompletableDeferred() }
        }

        val namespaces = ConcurrentHashMap<PackType, MutableSet<String>>()

        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] caching") }
        roots.asFlow().concurrent().collect { root -> consumeRoot(root, namespaces) }

        for ((type, deferred) in this@GenericPackResourcesCache.namespaces) {
            deferred.complete(namespaces[type] ?: emptySet())
        }
        allCompleted.complete()
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] cached") }
    }

    @OptIn(ExperimentalPathApi::class, ExperimentalCoroutinesApi::class)
    private suspend fun CoroutineScope.loadCache() =
        withContext(CoroutineName("Simple pack cache #${pack.packId()}")) {
            val time = measureTime {
                val hash by lazy {
                    (pack as HashablePackResources).`lazyyyyy$getHash`()
                }
                if (pack is HashablePackResources && hash != null) {
                    val root = roots.single()
                    val cachedData = PackResourcesCacheManager.getOrCache(hash!!)
                    if (cachedData != null) {
                        joinAll(
                            launch {
                                cachedData.files.asSequence().asFlow().concurrent().collect { (key, value) ->
                                    files[key] = CompletableDeferred(root.resolve(value))
                                }
                            },
                            launch {
                                cachedData.directoryToFiles.asSequence().asFlow().concurrent().collect { (key, value) ->
                                    directoryToFiles[key] = CompletableDeferred(value.mapKeys { root.resolve(it.key) })
                                }
                            },
                            launch {
                                cachedData.namespaces.asSequence().asFlow().concurrent().collect { (key, value) ->
                                    namespaces[key] = CompletableDeferred(value)
                                }
                            }
                        )
                        allCompleted.complete()
                    } else {
                        cachePack()
                        val deferredFiles = async { files.mapValues { it.value.getCompleted().toString() } }
                        val deferredDirectoryToFiles = async {
                            directoryToFiles.mapValues {
                                it.value.getCompleted().mapKeys { it.key.toString() }
                            }
                        }
                        val deferredNamespaces = async { namespaces.mapValues { it.value.getCompleted() } }
                        joinAll(deferredFiles, deferredDirectoryToFiles, deferredNamespaces)

                        @Suppress("UNCHECKED_CAST")
                        val data = PackResourcesCacheData(
                            deferredFiles.getCompleted(),
                            deferredDirectoryToFiles.getCompleted(),
                            deferredNamespaces.getCompleted()
                        )
                        PackResourcesCacheManager.cache[hash!!] = data
                        PackResourcesCacheManager.save(hash!!, data)
                    }
                } else {
                    cachePack()
                }
            }
            Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?): Set<String> {
        if (type == null) return emptySet()
        val deferred = namespaces[type] ?: return emptySet()
        if (deferred.isCompleted) return deferred.getCompleted()
        if (allCompleted.isCompleted) return emptySet()
        return runBlocking { deferred.await() }
    }
}