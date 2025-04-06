package settingdust.lazyyyyy.minecraft.pack_resources_cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

open class GenericPackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    var namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init {
        scope.launch { loadCache() }
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
                if (pack is HashablePackResources) {
                    val hash = pack.`lazyyyyy$getHash`()
                    val cachedData = PackResourcesCacheManager.getOrCache(hash)
                    if (cachedData != null) {
                        joinAll(
                            launch {
                                cachedData.files.asSequence().asFlow().concurrent().collect { (key, value) ->
                                    files[key] = CompletableDeferred(value)
                                }
                            },
                            launch {
                                cachedData.directoryToFiles.asSequence().asFlow().concurrent().collect { (key, value) ->
                                    directoryToFiles[key] = CompletableDeferred(value)
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
                        val values = awaitAll(
                            async { files.mapValues { it.value.getCompleted() } },
                            async { directoryToFiles.mapValues { it.value.getCompleted() } },
                            async { namespaces.mapValues { it.value.getCompleted() } }
                        )

                        @Suppress("UNCHECKED_CAST")
                        val data = PackResourcesCacheData(
                            values[0] as Map<String, Path>,
                            values[1] as Map<String, Map<Path, String>>,
                            values[2] as Map<PackType, Set<String>>
                        )
                        PackResourcesCacheManager.cache[hash] = data
                        PackResourcesCacheManager.save(hash, data)
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
        val deferred = namespaces.computeIfAbsent(type) { CompletableDeferred() }
        return (if (deferred.isCompleted) deferred.getCompleted()
        else runBlocking { namespaces[type]?.await() }) ?: emptySet()
    }
}