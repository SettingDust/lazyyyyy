package settingdust.lazyyyyy.game.pack_resources_cache

import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.minecraft.server.packs.PackType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream

object PackCacheStorage {
    private val gson = Gson()
    private val dir: Path = Paths.get(".cache", "lazyyyyy", "pack-cache")

    fun getCachePath(key: String, hashHex: String): Path {
        val fileName = "${key}_${hashHex}.json.gz".toValidFileName()
        return dir.resolve(fileName)
    }

    fun load(cachePath: Path): PackCacheSnapshot? {
        if (!cachePath.exists()) return null
        return try {
            GZIPInputStream(Files.newInputStream(cachePath)).use { input ->
                gson.fromJson(input.reader(), PackCacheSnapshot::class.java)
            }
        } catch (e: Exception) {
            PackCacheCore.logger.error("Failed to load pack cache from $cachePath", e)
            try {
                Files.deleteIfExists(cachePath)
            } catch (_: Exception) {
            }
            null
        }
    }

    fun save(data: PackCacheSnapshot, cachePath: Path) {
        try {
            cachePath.parent?.createDirectories()
            GZIPOutputStream(cachePath.outputStream()).use { output ->
                gson.toJson(data, output.writer())
            }
        } catch (e: Exception) {
            PackCacheCore.logger.error("Failed to save pack cache to $cachePath", e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun buildSnapshot(
        cache: PackCacheCore,
        namespacesSnapshot: Map<PackType, Set<String>>
    ): PackCacheSnapshot {
        val root = cache.roots.single()
        val files = mutableMapOf<String, String>()
        for ((key, deferred) in cache.files) {
            val path = deferred.getCompleted()
            files[key] = root.relativize(path).toString()
        }
        val directoryToFiles = mutableMapOf<String, MutableMap<String, String>>()
        for ((dirKey, deferred) in cache.directoryToFiles) {
            val map = deferred.getCompleted()
            val entry = mutableMapOf<String, String>()
            for ((path, relative) in map) {
                entry[root.relativize(path).toString()] = relative
            }
            directoryToFiles[dirKey] = entry
        }
        val namespaces = mutableMapOf<String, Set<String>>()
        for ((type, set) in namespacesSnapshot) {
            namespaces[type.name] = set
        }
        return PackCacheSnapshot(files, directoryToFiles, namespaces)
    }

    fun restoreSnapshot(
        cache: PackCacheCore,
        snapshot: PackCacheSnapshot,
        namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>>
    ) {
        val root = cache.roots.single()
        for ((key, relativePath) in snapshot.files) {
            val path = root.resolve(relativePath)
            cache.pathToRoot[path] = root
            cache.files[key] = CompletableDeferred(path)
        }
        val directoryToFiles = mutableMapOf<String, MutableMap<Path, String>>()
        for ((dirKey, entry) in snapshot.directoryToFiles) {
            val map = mutableMapOf<Path, String>()
            for ((relativePath, relative) in entry) {
                val path = root.resolve(relativePath)
                cache.pathToRoot[path] = root
                map[path] = relative
            }
            directoryToFiles[dirKey] = map
        }
        for ((dirKey, map) in directoryToFiles) {
            cache.directoryToFiles[dirKey] = CompletableDeferred(map)
        }
        for ((name, set) in snapshot.namespaces) {
            val type = PackType.valueOf(name)
            namespaces[type]?.complete(set)
        }
    }
}

private fun String.toValidFileName(replacement: String = "_"): String {
    val illegalChars = Regex("[/\\\\:*?\"<>|\\x00-\\x1F]")
    val windowsReserved = setOf("CON", "PRN", "AUX", "NUL", "COM1-9", "LPT1-9")
    val sanitized = this.replace(illegalChars, replacement)
    val prefixed =
        if (windowsReserved.any { sanitized.uppercase().startsWith(it) }) "$replacement$sanitized" else sanitized
    return prefixed.take(255)
}

internal fun ByteArray.toHexString(): String =
    joinToString("") { "%02x".format(it) }

