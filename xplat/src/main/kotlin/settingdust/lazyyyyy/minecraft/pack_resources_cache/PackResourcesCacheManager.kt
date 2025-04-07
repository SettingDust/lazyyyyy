package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object PackResourcesCacheManager {
    private val dir = Path(".lazyyyyy", "pack-cache")
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "_t"
    }

    val cache = ConcurrentHashMap<HashCode, PackResourcesCacheData>()

    fun getHash(file: File): HashCode = HashCode.fromBytes(DigestUtils.md5(file.inputStream().buffered()))

    fun getHash(path: Path): HashCode = HashCode.fromBytes(DigestUtils.md5(path.inputStream().buffered()))

    fun getOrCache(hashCode: HashCode): PackResourcesCacheData? {
        return cache[hashCode] ?: load(hashCode)?.also { cache[hashCode] = it }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(hashCode: HashCode): PackResourcesCacheData? {
        val path = dir.resolve("$hashCode.json.gz")
        if (!path.exists()) return null
        try {
            val data = GzipCompressorInputStream(path.inputStream()).use {
                json.decodeFromStream<PackResourcesCacheData>(it)
            }
            cache[hashCode] = data
            return data
        } catch (e: Exception) {
            PackResourcesCache.logger.error("Failed to load cache from $path", e)
            path.deleteExisting()
            return null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(hashCode: HashCode, data: PackResourcesCacheData) {
        val path = dir.resolve("$hashCode.json.gz")
        if (!path.parent.exists()) path.createParentDirectories()
        if (!path.exists()) path.createFile()
        GzipCompressorOutputStream(path.outputStream(StandardOpenOption.TRUNCATE_EXISTING)).use {
            json.encodeToStream(data, it)
        }
    }
}