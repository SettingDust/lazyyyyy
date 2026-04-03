package settingdust.lazyyyyy.game.pack_resources_cache

import kotlinx.coroutines.*
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.game.LazyyyyyMixinConfig
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class PackCache(
    pack: PackResources,
    roots: List<Path>,
    private val layout: PackCacheLayout = DefaultPackCacheLayout
) : PackCacheCore(pack, roots) {
    constructor(pack: PackResources, roots: List<Path>) : this(pack, roots, DefaultPackCacheLayout)
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    private val namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init {
        startLoad()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startLoad() {
        for (type in PackType.entries) {
            namespaces.computeIfAbsent(type) { CompletableDeferred() }
        }

        scope.launch(CoroutineName("Pack cache")) {
            try {
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
            } catch (e: Exception) {
                logger.error("Error loading pack cache for ${pack.packId()}", e)
                // Fallback: ensure all deferreds are completed to prevent permanent blocking
                for ((_, deferred) in namespaces) {
                    if (!deferred.isCompleted) deferred.completeExceptionally(e)
                }
                if (!allCompleted.isCompleted) allCompleted.completeExceptionally(e)
            }
        }
    }

    private suspend fun buildCache() {
        try {
            val result = layout.cachePack(this@PackCache)
            // 立即 complete namespace deferreds
            for ((type, deferred) in namespaces) {
                deferred.complete(result.namespaces[type] ?: emptySet())
            }
            // Wait for file indexing to complete
            result.awaitIndexing()
            allCompleted.complete()
        } catch (e: Exception) {
            logger.error("Error building pack cache for ${pack.packId()}", e)
            // Fallback: ensure all deferreds are completed to prevent permanent blocking
            for ((_, deferred) in namespaces) {
                deferred.completeExceptionally(e)
            }
            allCompleted.completeExceptionally(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?): Set<String> {
        if (type == null) return emptySet()
        val deferred = namespaces[type] ?: return emptySet()
        if (deferred.isCompleted) return try { deferred.getCompleted() } catch (_: Exception) { emptySet() }
        if (allCompleted.isCompleted) return emptySet()
        return try { runBlocking { deferred.await() } } catch (_: Exception) { emptySet() }
    }

}
