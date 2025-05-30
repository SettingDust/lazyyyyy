package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.collect.HashBiMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.PlatformService
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy.PackRoot
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCacheManager.toValidFileName
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class GenericPackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    var namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init { loadCache() }

    private suspend fun consumeRoot(
        root: Path,
        namespaces: ConcurrentHashMap<PackType, MutableSet<String>>
    ) = coroutineScope  {
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
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
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
            this@GenericPackResourcesCache.directoryToFiles[path]!!.complete(files)
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/directoryToFiles] cached") }

        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] cached") }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] cached") }
    }

    private suspend fun cachePack() = coroutineScope  {
        val namespaces = ConcurrentHashMap<PackType, MutableSet<String>>()

        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] caching") }
        roots.asFlow().concurrent().collect { root -> consumeRoot(root, namespaces) }

        for ((type, deferred) in this@GenericPackResourcesCache.namespaces) {
            deferred.complete(namespaces[type] ?: emptySet())
        }
        allCompleted.complete()
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] cached") }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    private fun loadCache() {
        for (type in PackType.entries) {
            namespaces.computeIfAbsent(type) { CompletableDeferred() }
        }

        scope.launch(CoroutineName("Simple pack cache #${pack.packId()}")) {
            val time = measureTime {
                val hash by lazy { (pack as HashablePackResources).`lazyyyyy$getHash`() }

                if (pack is HashablePackResources && hash != null) {
                    val rootHashes =
                        async {
                            roots.associateWithTo(HashBiMap.create(roots.size)) {
                                PlatformService.getPathHash(it).toByteArray().toHexString()
                            }
                        }
                    val key = pack.packId()
                    val lock = PackResourcesCacheManager.getLock(key)
                    lock.lock(this@GenericPackResourcesCache)
                    val cachePath =
                        PackResourcesCacheManager.dir.resolve("${key}_${hash!!.toHexString()}.json.gz".toValidFileName())
                    val cachedDataDeferred = PackResourcesCacheManager.get(key, cachePath)
                    rootHashes.join()

                    @OptIn(ExperimentalCoroutinesApi::class)
                    suspend fun cacheEntry() {
                        cachePack()
                        val roots = ConcurrentHashMap<String, PackResourcesCacheDataEntry>()
                        for ((_, rootHash) in rootHashes.getCompleted()) {
                            roots[rootHash] = PackResourcesCacheDataEntry(ConcurrentHashMap(), ConcurrentHashMap())
                        }

                        val deferredNamespaces = async { namespaces.mapValues { it.value.getCompleted() } }

                        joinAll(
                            launch { filesToCache(roots, rootHashes.getCompleted()) },
                            launch { directoryToFilesToCache(roots, rootHashes.getCompleted()) },
                            deferredNamespaces
                        )

                        PackResourcesCacheManager.save(
                            key,
                            PackResourcesCacheData(
                                roots,
                                deferredNamespaces.getCompleted()
                            ),
                            cachePath
                        )
                        lock.unlock(this@GenericPackResourcesCache)
                    }

                    if (cachedDataDeferred.isCompleted) {
                        lock.unlock(this@GenericPackResourcesCache)
                        val cachedData = cachedDataDeferred.getCompleted()

                        val directoryToFiles =
                            ConcurrentHashMap<String, MutableMap<Path, String>>()
                        try {
                            if (!cachedData.roots.keys.containsAll(rootHashes.getCompleted().values)) {
                                throw IllegalStateException("Missing roots in cache. Cached: ${cachedData.roots.keys}. Requested: ${rootHashes.getCompleted().values}")
                            }
                            joinAll(
                                launch {
                                    cachedData.namespaces.asSequence().asFlow().concurrent()
                                        .collect { (key, value) ->
                                            namespaces[key]!!.complete(value)
                                        }
                                },
                                launch {
                                    cachedData.roots.asSequence().asFlow().concurrent()
                                        .collect { (rootHash, entry) ->
                                            val root = rootHashes.getCompleted().inverse()[rootHash] ?: return@collect

                                            joinAll(
                                                launch {
                                                    entry.files.asSequence().asFlow().concurrent()
                                                        .collect { (key, value) ->
                                                            val path = root.resolve(value)
                                                            pathToRoot[path] = root
                                                            files[key] = CompletableDeferred(path)
                                                        }
                                                },
                                                launch {
                                                    entry.directoryToFiles.asSequence().asFlow().concurrent()
                                                        .collect { (key, value) ->
                                                            directoryToFiles.computeIfAbsent(key) { ConcurrentHashMap() } += value.mapKeys {
                                                                val path = root.resolve(it.key)
                                                                pathToRoot[path] = root
                                                                path
                                                            }
                                                        }
                                                }
                                            )
                                        }
                                }
                            )

                            for (it in directoryToFiles) {
                                this@GenericPackResourcesCache.directoryToFiles[it.key] =
                                    CompletableDeferred(it.value)
                            }
                        } catch (e: Exception) {
                            logger.error("Error loading pack disk cache ${pack.packId()}. Re-create cache", e)
                            lock.lock(this@GenericPackResourcesCache)
                            cacheEntry()
                        }
                        allCompleted.complete()
                    } else {
                        cacheEntry()
                    }
                } else {
                    cachePack()
                }
            }
            Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
        }
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