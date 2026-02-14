package settingdust.lazyyyyy.game.util.pack_resources_cache

import com.dynatrace.hash4j.file.FileHashing
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object HashManager {
    private val logger = LogManager.getLogger()
    private val caches = ConcurrentHashMap<HashCacheType, ConcurrentHashMap<String, ByteArray>>()

    @JvmOverloads
    fun getFileHash(path: Path, type: HashCacheType = HashCacheType.Generic): ByteArray {
        return try {
            val key = path.toAbsolutePath().toString()
            val cache = caches.computeIfAbsent(type) { ConcurrentHashMap() }
            val cached = cache[key]
            if (cached != null) return cached
            val hash = FileHashing.imohash1_0_2().hashFileTo128Bits(path)
            val bytes = hash.toString().hexToBytes()
            cache[key] = bytes
            bytes
        } catch (e: Exception) {
            logger.warn("Failed to calculate path hash for {}", path, e)
            ByteArray(0)
        }
    }

    fun invalidateAll() {
        caches.clear()
    }

    fun invalidate(type: HashCacheType) {
        caches[type]?.clear()
    }
}

enum class HashCacheType {
    ResourcePack,
    DataPack,
    Generic
}

private fun String.hexToBytes(): ByteArray {
    val len = length
    if (len % 2 != 0) return ByteArray(0)
    val result = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        result[i / 2] = substring(i, i + 2).toInt(16).toByte()
        i += 2
    }
    return result
}