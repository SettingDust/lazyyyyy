package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
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
    val dir = Path(".lazyyyyy", "pack-cache")
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "_t"
    }

    val cache = ConcurrentHashMap<Pair<String, HashCode>, CompletableDeferred<PackResourcesCacheData>>()
    val cacheLocks = ConcurrentHashMap<Pair<String, HashCode>, Mutex>()

    fun getHash(file: File): HashCode = HashCode.fromBytes(DigestUtils.md5(file.inputStream().buffered()))

    fun getHash(path: Path): HashCode = HashCode.fromBytes(DigestUtils.md5(path.inputStream().buffered()))

    fun getLock(key: Pair<String, HashCode>) =
        cacheLocks.computeIfAbsent(key) { Mutex() }

    fun get(key: Pair<String, HashCode>, cachePath: Path): CompletableDeferred<PackResourcesCacheData> {
        val deferred = cache.computeIfAbsent(key) { CompletableDeferred() }
        if (!deferred.isCompleted) {
            val data = load(cachePath)
            if (data != null) deferred.complete(data)
        }
        return deferred
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(cachePath: Path): PackResourcesCacheData? {
        if (!cachePath.exists()) return null
        try {
            return GzipCompressorInputStream(cachePath.inputStream())
                .use { json.decodeFromStream<PackResourcesCacheData>(it) }
        } catch (e: Exception) {
            PackResourcesCache.logger.error("Failed to load cache from $cachePath", e)
            cachePath.deleteExisting()
            return null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(key: Pair<String, HashCode>, data: PackResourcesCacheData, cachePath: Path) {
        cache[key]!!.complete(data)
        if (!cachePath.parent.exists()) cachePath.createParentDirectories()
        if (!cachePath.exists()) cachePath.createFile()
        GzipCompressorOutputStream(cachePath.outputStream(StandardOpenOption.TRUNCATE_EXISTING))
            .use { json.encodeToStream(data, it) }
    }

    fun String.toValidFileName(replacement: String = "_"): String {
        val illegalChars = Regex("[/\\\\:*?\"<>|\\x00-\\x1F]")
        val windowsReserved = setOf("CON", "PRN", "AUX", "NUL", "COM1-9", "LPT1-9")

        return this
            .replace(illegalChars, replacement)
            .let { name -> if (windowsReserved.any { name.uppercase().startsWith(it) }) "$replacement$name" else name }
            .take(255)
    }
}