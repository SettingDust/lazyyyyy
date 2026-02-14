package settingdust.lazyyyyy.game.pack_resources_cache

import net.minecraft.server.packs.PackType

interface PackCacheLayout {
    suspend fun cachePack(cache: PackCacheCore): Map<PackType, Set<String>>
}

