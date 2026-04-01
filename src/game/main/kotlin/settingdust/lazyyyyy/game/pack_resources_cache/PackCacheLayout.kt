package settingdust.lazyyyyy.game.pack_resources_cache

import net.minecraft.server.packs.PackType

interface PackCacheLayout {
    suspend fun cachePack(cache: PackCacheCore): PackCacheLayoutResult
}

class PackCacheLayoutResult(
    val namespaces: Map<PackType, Set<String>>,
    val awaitIndexing: suspend () -> Unit
)

