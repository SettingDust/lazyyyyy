package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.collect.HashBiMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.DetectedVersion
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.PlatformService
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCacheManager.toValidFileName
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import settingdust.lazyyyyy.util.flatMap
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class VanillaPackResourcesCache(
    pack: PackResources,
    roots: List<Path>,
    private val pathsForType: Map<PackType, List<Path>>
) : PackResourcesCache(pack, roots) {
    companion object {
        val HASH = DetectedVersion.BUILT_IN.dataVersion.version.toLong()
    }

    init {
        scope.launch { loadCache() }
    }

    private suspend fun CoroutineScope.consumeRoot(root: Path) {
        val strategy = CachingStrategy.PackRoot(root, null)
        val blacklisted = packTypeByDirectory.keys + listOf("net", "com", "coremods", "META-INF", "minecraft", "realms")
        root.listDirectoryEntries().asFlow().concurrent().collect { path ->
            val relativePath = root.relativize(path)
            val firstPath = relativePath.firstOrNull()
            if (firstPath?.name in blacklisted) return@collect
            if (path.isDirectory()) {
                consumeRootDirectory(this, path, strategy)
            } else {
                consumeFile(this, path, strategy)
            }
        }
    }

    suspend fun PackResourcesCache.consumePackType(
        strategy: CachingStrategy,
        directoryToFiles: MutableMap<String, MutableMap<Path, String>>
    ) {
        coroutineScope {
            val entries = strategy.root.listDirectoryEntries()
            entries.asFlow().concurrent().collect { path ->
                if (path.isDirectory()) {
                    consumeResourceDirectory(path, directoryToFiles, strategy)
                } else {
                    consumeFile(this, path, strategy)
                }
            }
        }
    }

    @OptIn(ExperimentalPathApi::class, ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    private suspend fun CoroutineScope.loadCache() =
        withContext(CoroutineName("Vanilla pack cache #${pack.packId()}")) {
            val time = measureTime {
                require(pack is HashablePackResources)
                val rootHashes =
                    async {
                        (pathsForType.values.flatMap { it } + roots).toSet()
                            .associateWithTo(HashBiMap.create()) { PlatformService.getPathHash(it).toLong() }
                    }
                val key = pack.packId()
                val lock = PackResourcesCacheManager.getLock(key)
                lock.lock(this@VanillaPackResourcesCache)
                val cachePath =
                    PackResourcesCacheManager.dir.resolve("${key}_${HASH.toHexString()}.json.gz".toValidFileName())
                val cachedDataDeferred = PackResourcesCacheManager.get(key, cachePath)
                rootHashes.join()
                @OptIn(ExperimentalCoroutinesApi::class)
                suspend fun cacheEntry() {
                    cachePack()
                    val roots = ConcurrentHashMap<Long, PackResourcesCacheDataEntry>()
                    for ((_, hash) in rootHashes.getCompleted()) {
                        roots[hash] = PackResourcesCacheDataEntry(ConcurrentHashMap(), ConcurrentHashMap())
                    }

                    joinAll(
                        launch { filesToCache(roots, rootHashes.getCompleted()) },
                        launch { directoryToFilesToCache(roots, rootHashes.getCompleted()) }
                    )

                    PackResourcesCacheManager.save(key, PackResourcesCacheData(roots), cachePath)
                    lock.unlock(this@VanillaPackResourcesCache)
                }

                if (cachedDataDeferred.isCompleted) {
                    lock.unlock(this@VanillaPackResourcesCache)
                    val cachedData = cachedDataDeferred.getCompleted()
                    try {
                        val directoryToFiles =
                            ConcurrentHashMap<String, MutableMap<Path, String>>()

                        cachedData.roots.asSequence().asFlow().concurrent().collect { (rootHash, entry) ->
                            val root = rootHashes.getCompleted().inverse()[rootHash]
                                ?: error("No valid root for ${pack.packId()} $rootHash")

                            joinAll(
                                launch {
                                    entry.files.asSequence().asFlow().concurrent().collect { (key, value) ->
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

                        for (it in directoryToFiles) {
                            this@VanillaPackResourcesCache.directoryToFiles[it.key] = CompletableDeferred(it.value)
                        }
                        allCompleted.complete()
                    } catch (e: Exception) {
                        logger.error("Error loading pack cache ${pack.packId()}#$HASH. Re-create cache", e)
                        lock.lock(this@VanillaPackResourcesCache)
                        cacheEntry()
                    }
                } else {
                    cacheEntry()
                }
            }
            Lazyyyyy.logger.debug("Cache vanilla pack ${pack.packId()} in $time")
        }

    private suspend fun CoroutineScope.cachePack() {
        joinAll(
            launch {
                val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
                pathsForType.asSequence().asFlow().concurrent()
                    .flatMap { (type, paths) -> paths.asFlow().map { type to it } }
                    .collect { (type, packTypeRoot) ->
                        val strategy = CachingStrategy.PackTypeRoot(packTypeRoot, type.directory)
                        consumePackType(strategy, directoryToFiles)
                    }
                for ((path, files) in directoryToFiles) {
                    this@VanillaPackResourcesCache.directoryToFiles[path]!!.complete(files)
                }
            },
            launch { roots.asFlow().concurrent().collect { consumeRoot(it) } }
        )
        allCompleted.complete()
    }

    override fun getNamespaces(type: PackType?): Set<String> {
        // Vanilla cached the namespace when constructing
        throw UnsupportedOperationException()
    }
}