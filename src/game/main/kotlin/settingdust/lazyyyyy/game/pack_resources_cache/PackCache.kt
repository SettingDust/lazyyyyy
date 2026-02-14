package settingdust.lazyyyyy.game.pack_resources_cache

import kotlinx.coroutines.*
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.game.LazyyyyyMixinConfig
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class PackCache(
    pack: PackResources,
    roots: List<Path>,
    private val layout: PackCacheLayout = DefaultPackCacheLayout()
) : PackCacheCore(pack, roots) {
    constructor(pack: PackResources, roots: List<Path>) : this(pack, roots, DefaultPackCacheLayout())
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    private val namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init {
        startLoad()
    }

    private fun startLoad() {
        for (type in PackType.entries) {
            namespaces.computeIfAbsent(type) { CompletableDeferred() }
        }

        scope.launch(CoroutineName("Pack cache")) {
            if (!LazyyyyyMixinConfig.isFeatureEnabled(LazyyyyyMixinConfig.FEATURE_PACK_RESOURCES_CACHE_PERSISTENCE)) {
                buildCache()
                return@launch
            }
            val hashable = pack as? PackCacheHashProvider
            val hash = hashable?.`lazyyyyy$getHash`()
            if (hash == null || roots.size != 1) {
                buildCache()
                return@launch
            }

            val key = pack.javaClass.name
            val cachePath = PackCacheStorage.getCachePath(key, hash.toHexString())
            val cached = PackCacheStorage.load(cachePath)
            if (cached != null) {
                PackCacheStorage.restoreSnapshot(this@PackCache, cached, namespaces)
                allCompleted.complete()
            } else {
                buildCache()
                val snapshot = namespaces.mapValues { it.value.getCompleted() }
                val data = PackCacheStorage.buildSnapshot(this@PackCache, snapshot)
                PackCacheStorage.save(data, cachePath)
            }
        }
    }

    private suspend fun buildCache() {
        val snapshot = layout.cachePack(this@PackCache)
        for ((type, deferred) in namespaces) {
            deferred.complete(snapshot[type] ?: emptySet())
        }
        allCompleted.complete()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?): Set<String> {
        if (type == null) return emptySet()
        val deferred = namespaces[type] ?: return emptySet()
        if (deferred.isCompleted) return deferred.getCompleted()
        if (allCompleted.isCompleted) return emptySet()
        return runBlocking { deferred.await() }
    }

    private class DefaultPackCacheLayout : PackCacheLayout {
        override suspend fun cachePack(cache: PackCacheCore): Map<PackType, Set<String>> = coroutineScope {
            val results = cache.roots.map { root ->
                async { scanRoot(cache, root) }
            }.awaitAll()
            mergeNamespaces(results)
        }

        private suspend fun scanRoot(
            cache: PackCacheCore,
            root: Path
        ): Map<PackType, Set<String>> = coroutineScope {
            val namespaces = ConcurrentHashMap<PackType, MutableSet<String>>()
            val rootStrategy = PackCachePathStrategy.PackRoot(root, null)
            root.listDirectoryEntries().map { path ->
                async {
                    if (path.isDirectory()) {
                        val relativePath = root.relativize(path)
                        val firstPath = relativePath.firstOrNull()
                        val packType = packTypeByDirectory[firstPath?.name] ?: return@async
                        val set = scanPackType(cache, path, PackCachePathStrategy.PackRoot(root, path))
                        namespaces.computeIfAbsent(packType) { ConcurrentHashMap.newKeySet() }.addAll(set)
                    } else {
                        cache.indexFile(this, path, rootStrategy)
                    }
                }
            }.awaitAll()
            namespaces.mapValues { it.value }
        }

        private suspend fun scanPackType(
            cache: PackCacheCore,
            directory: Path,
            strategy: PackCachePathStrategy
        ): Set<String> = coroutineScope {
            val namespaces = ConcurrentHashMap.newKeySet<String>()
            val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
            directory.listDirectoryEntries().map { path ->
                async {
                    if (path.isDirectory()) {
                        namespaces += path.name
                        cache.indexResourceDirectory(path, directoryToFiles, strategy)
                    } else {
                        cache.indexFile(this, path, strategy)
                    }
                }
            }.awaitAll()
            for ((path, files) in directoryToFiles) {
                cache.directoryToFiles[path]!!.complete(files)
            }
            namespaces
        }

        private fun mergeNamespaces(
            results: List<Map<PackType, Set<String>>>
        ): Map<PackType, Set<String>> {
            val merged = ConcurrentHashMap<PackType, MutableSet<String>>()
            for (result in results) {
                for ((type, set) in result) {
                    merged.computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }.addAll(set)
                }
            }
            return merged.mapValues { it.value }
        }
    }
}
