package settingdust.lazyyyyy.game.util.pack_resources_cache

import net.lenni0451.reflect.stream.RStream
import settingdust.lazyyyyy.util.ServiceLoaderUtil
import java.nio.file.Files
import java.nio.file.Path

interface FileSystemPathAdapter {
    companion object : FileSystemPathAdapter {
        private val implementations by lazy {
            ServiceLoaderUtil.findServices(FileSystemPathAdapter::class.java)
        }

        override fun getFileSystemPath(path: Path): Path? {
            for (adapter in implementations) {
                val result = adapter.getFileSystemPath(path)
                if (result != null) {
                    return result
                }
            }
            return null
        }
    }

    fun getFileSystemPath(path: Path): Path?
}

class RegularFilePathAdapter : FileSystemPathAdapter {
    override fun getFileSystemPath(path: Path): Path? {
        if (Files.isRegularFile(path)) return path;
        return null
    }
}

class ZipFileSystemPathAdapter : FileSystemPathAdapter {
    private val ZipFileSystemClass by lazy { Class.forName("jdk.nio.zipfs.ZipFileSystem") }
    private val ZipFileSystemStream by lazy { RStream.of(ZipFileSystemClass) }
    private val getZipFileMethod by lazy {
        ZipFileSystemStream.methods().by("getZipFile")
    }

    override fun getFileSystemPath(path: Path): Path? {
        if (ZipFileSystemClass.isInstance(path.fileSystem)) {
            return getZipFileMethod.invokeInstance(path.fileSystem)
        }
        return null
    }
}

fun Path.getFileSystemPath(): Path? = FileSystemPathAdapter.getFileSystemPath(this)
