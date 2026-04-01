package settingdust.lazyyyyy.game.util.pack_resources_cache

import com.google.common.base.Suppliers
import net.lenni0451.reflect.stream.RStream
import settingdust.lazyyyyy.util.ServiceLoaderUtil
import java.nio.file.Path
import java.util.ServiceLoader
import java.util.function.Supplier

interface FileSystemPathAdapter {
    companion object {
        @JvmStatic
        val supplier: Supplier<FileSystemPathAdapter> = Suppliers.memoize {
            ServiceLoaderUtil.findService(
                FileSystemPathAdapter::class.java,
                ServiceLoader.load(FileSystemPathAdapter::class.java, FileSystemPathAdapter::class.java.classLoader)
            )
        }

        @JvmStatic
        fun get(): FileSystemPathAdapter = supplier.get()
    }

    fun getFileSystemPath(path: Path): Path?
}

object DefaultFileSystemPathAdapter : FileSystemPathAdapter {
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

fun Path.getFileSystemPath(): Path? = FileSystemPathAdapter.get().getFileSystemPath(this)
