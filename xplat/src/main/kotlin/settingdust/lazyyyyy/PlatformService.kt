package settingdust.lazyyyyy

import settingdust.lazyyyyy.minecraft.pack_resources_cache.getZipFileSystemPath
import java.nio.file.Path
import java.util.*
import kotlin.io.path.pathString

interface PlatformService {
    companion object : PlatformService by ServiceLoader.load(PlatformService::class.java).first()

    val configDir: Path

    fun isModLoaded(modId: String): Boolean

    fun hasEarlyError(): Boolean

    fun getFileSystemPath(path: Path): Path? = path.getZipFileSystemPath()

    fun getPathHash(path: Path): Int {
        var result = path.pathString.hashCode()
        getFileSystemPath(path)?.let {
            result = Objects.hash(result, it.pathString.hashCode())
        }
        return result
    }
}