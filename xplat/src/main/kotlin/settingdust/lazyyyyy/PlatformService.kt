package settingdust.lazyyyyy

import com.dynatrace.hash4j.hashing.HashValue128
import com.dynatrace.hash4j.hashing.Hashing
import settingdust.lazyyyyy.minecraft.pack_resources_cache.getZipFileSystemPath
import java.nio.file.Path
import java.util.*
import kotlin.io.path.pathString

val minecraftHasEarlyError by lazy {
    try {
        PlatformService.hasEarlyError()
    } catch (e: Throwable) {
        Lazyyyyy.logger.error(e)
        true
    }
}

interface PlatformService {
    companion object : PlatformService by ServiceLoader.load(PlatformService::class.java).firstOrNull()
        ?: error("No platform service found")

    val configDir: Path

    fun isModLoaded(modId: String): Boolean

    fun hasEarlyError(): Boolean

    fun getFileSystemPath(path: Path): Path? = path.getZipFileSystemPath()

    fun getPathHash(path: Path): HashValue128 {
        val hashStream = Hashing.xxh3_128().hashStream()
        hashStream.putString(path.pathString)
        getFileSystemPath(path)?.let {
            hashStream.putString(it.pathString)
        }
        return hashStream.get()
    }
}