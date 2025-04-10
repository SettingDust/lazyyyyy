package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.collect.HashBiMap
import com.google.common.hash.HashCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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
import settingdust.lazyyyyy.PlatformService
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy.PackRoot
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCacheManager.toValidFileName
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
                consumePackType(root, packType, path, PackRoot(root, path), namespaces)
            } else {
                consumeFile(this, root, path, strategy)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#root/$root] cached") }
    }

    private suspend fun CoroutineScope.consumePackType(
        root: Path,
        type: PackType,
        directory: Path,
        strategy: CachingStrategy,
        namespaces: MutableMap<PackType, MutableSet<String>>
    ) {
        namespaces.computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] caching") }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] caching") }
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Pair<Path, Path>, String>>()
        directory.listDirectoryEntries().asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                namespaces[type]!! += path.name
                consumeResourceDirectory(root, path, directoryToFiles, strategy)
            } else {
                consumeFile(this, root, path, strategy)
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
                val hash by lazy { (pack as HashablePackResources).`lazyyyyy$getHash`() }

                if (pack is HashablePackResources && hash != null) {
                    val rootHashes =
                        async {
                            roots.associateWithTo(HashBiMap.create(roots.size)) {
                                HashCode.fromInt(PlatformService.getPathHash(it))
                            }
                        }
                    val key = pack.packId() to hash!!
                    val lock = PackResourcesCacheManager.getLock(key)
                    lock.lock(this@GenericPackResourcesCache)
                    val cachePath =
                        PackResourcesCacheManager.dir.resolve("${key.first}-${key.second}.json.gz".toValidFileName())
                    val cachedDataDeferred = PackResourcesCacheManager.get(key, cachePath)
                    rootHashes.join()

                    @OptIn(ExperimentalCoroutinesApi::class)
                    suspend fun cacheEntry() {
                        cachePack()
                        val roots = ConcurrentHashMap<HashCode, PackResourcesCacheDataEntry>()
                        for ((_, hash) in rootHashes.getCompleted()) {
                            roots[hash] = PackResourcesCacheDataEntry(ConcurrentHashMap(), ConcurrentHashMap())
                        }
                        val deferredFiles = async { filesToCache(roots, rootHashes.getCompleted()) }
                        val deferredDirectoryToFiles =
                            async { directoryToFilesToCache(roots, rootHashes.getCompleted()) }
                        val deferredNamespaces = async { namespaces.mapValues { it.value.getCompleted() } }

                        joinAll(deferredFiles, deferredDirectoryToFiles, deferredNamespaces)

                        PackResourcesCacheManager.save(
                            key,
                            PackResourcesCacheData(roots, deferredNamespaces.getCompleted()),
                            cachePath
                        )
                        lock.unlock(this@GenericPackResourcesCache)
                    }

                    if (cachedDataDeferred.isCompleted) {
                        lock.unlock(this@GenericPackResourcesCache)
                        val cachedData = cachedDataDeferred.getCompleted()

                        val directoryToFiles =
                            ConcurrentHashMap<String, MutableMap<Pair<Path, Path>, String>>()
                        try {
                            joinAll(
                                launch {
                                    cachedData.namespaces.asSequence().asFlow().concurrent().collect { (key, value) ->
                                        namespaces[key] = CompletableDeferred(value)
                                    }
                                },
                                launch {
                                    cachedData.roots.asSequence().asFlow().concurrent().collect { (rootHash, entry) ->
                                        val root = rootHashes.getCompleted().inverse()[rootHash]
                                            ?: error("No valid root for ${pack.packId()} $rootHash")

                                        joinAll(
                                            launch {
                                                entry.files.asSequence().asFlow().concurrent().collect { (key, value) ->
                                                    this@GenericPackResourcesCache.files[key] =
                                                        CompletableDeferred(root to root.resolve(value))
                                                }
                                            },
                                            launch {
                                                entry.directoryToFiles.asSequence().asFlow().concurrent()
                                                    .collect { (key, value) ->
                                                        directoryToFiles.computeIfAbsent(key) { ConcurrentHashMap() } += value.mapKeys {
                                                            root to root.resolve(
                                                                it.key
                                                            )
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
                            logger.error("Error loading pack cache ${pack.packId()}#$hash. Re-create cache", e)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?): Set<String> {
        if (type == null) return emptySet()
        val deferred = namespaces[type] ?: return emptySet()
        if (deferred.isCompleted) return deferred.getCompleted()
        if (allCompleted.isCompleted) return emptySet()
        return runBlocking { deferred.await() }
    }
}