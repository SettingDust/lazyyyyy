package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.minecraft.server.packs.PackResources
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

    val cache = ConcurrentHashMap<Pair<String, HashCode>, PackResourcesCacheData>()

    fun getHash(file: File): HashCode = HashCode.fromBytes(DigestUtils.md5(file.inputStream().buffered()))

    fun getHash(path: Path): HashCode = HashCode.fromBytes(DigestUtils.md5(path.inputStream().buffered()))

    fun getOrCache(pack: PackResources, hashCode: HashCode): PackResourcesCacheData? {
        val key = pack.packId() to hashCode
        return cache[key] ?: load(pack, hashCode)?.also { cache[key] = it }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(pack: PackResources, hashCode: HashCode): PackResourcesCacheData? {
        val path = dir.resolve("${pack.packId()}-$hashCode.json.gz".toValidFileName())
        if (!path.exists()) return null
        try {
            val data = GzipCompressorInputStream(path.inputStream()).use {
                json.decodeFromStream<PackResourcesCacheData>(it)
            }
            cache[pack.packId() to hashCode] = data
            return data
        } catch (e: Exception) {
            PackResourcesCache.logger.error("Failed to load cache from $path", e)
            path.deleteExisting()
            return null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(pack: PackResources, hashCode: HashCode, data: PackResourcesCacheData) {
        cache[pack.packId() to hashCode] = data
        val path = dir.resolve("${pack.packId()}-$hashCode.json.gz".toValidFileName())
        if (!path.parent.exists()) path.createParentDirectories()
        if (!path.exists()) path.createFile()
        GzipCompressorOutputStream(path.outputStream(StandardOpenOption.TRUNCATE_EXISTING)).use {
            json.encodeToStream(data, it)
        }
    }

    private fun String.toValidFileName(replacement: String = "_"): String {
        val illegalChars = Regex("[/\\\\:*?\"<>|\\x00-\\x1F]")
        val windowsReserved = setOf("CON", "PRN", "AUX", "NUL", "COM1-9", "LPT1-9")

        return this
            .replace(illegalChars, replacement)
            .let { name -> if (windowsReserved.any { name.uppercase().startsWith(it) }) "$replacement$name" else name }
            .take(255)
    }
}