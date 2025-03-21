package settingdust.lazyyyyy.minecraft.pack_resources_cache

import java.nio.file.Path


interface CachingStrategy {
    val root: Path

    fun getRootRelativePath(path: Path): Path = root.relativize(path)
    fun getNamespaceRoot(path: Path): Path?
    fun getRelativePathString(path: Path) = PackResourcesCache.JOINER.join(path)

    class PackRoot(override val root: Path, val packTypeRoot: Path?) : CachingStrategy {
        override fun getNamespaceRoot(path: Path): Path? = try {
            path.subpath(0, packTypeRoot!!.nameCount + 1)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    class PackTypeRoot(override val root: Path, val type: String) : CachingStrategy {
        override fun getNamespaceRoot(path: Path): Path? = try {
            path.subpath(0, root.nameCount + 1)
        } catch (_: IllegalArgumentException) {
            null
        }

        override fun getRelativePathString(path: Path) = "$type/${super.getRelativePathString(path)}"
    }
}