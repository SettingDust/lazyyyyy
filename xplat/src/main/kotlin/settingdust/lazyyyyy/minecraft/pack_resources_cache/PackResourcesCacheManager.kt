package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createFile
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

    fun getOrCache(hashCode: HashCode): PackResourcesCacheData? {
        return cache[hashCode] ?: load(hashCode)?.also { cache[hashCode] = it }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(hashCode: HashCode): PackResourcesCacheData? {
        val path = dir.resolve("$hashCode.json.gz")
        if (!path.exists()) return null
        val data = json.decodeFromStream<PackResourcesCacheData>(GZIPInputStream(path.inputStream()))
        cache[hashCode] = data
        return data
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(hashCode: HashCode, data: PackResourcesCacheData) {
        val path = dir.resolve("$hashCode.json.gz")
        runCatching { path.createFile() }
        json.encodeToStream(data, GZIPOutputStream(path.outputStream()))
    }
}