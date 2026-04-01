package settingdust.lazyyyyy.game.pack_resources_cache

import java.nio.file.Path

interface PackCachePathStrategy {
    val root: Path

    fun getRootRelativePath(path: Path): Path = root.relativize(path)
    fun getNamespaceRoot(path: Path): Path?
    fun getRelativePathString(path: Path) = PackCacheCore.Companion.JOINER.join(path)

    class PackRoot(override val root: Path, val packTypeRoot: Path?) : PackCachePathStrategy {
        override fun getNamespaceRoot(path: Path): Path? {
            val ptr = packTypeRoot ?: return null
            return try {
                path.subpath(0, ptr.nameCount + 1).let { ptr.root?.resolve(it) ?: it }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    class PackTypeRoot(override val root: Path, val type: String) : PackCachePathStrategy {
        override fun getNamespaceRoot(path: Path): Path? = try {
            path.subpath(0, root.nameCount + 1).let { root.root?.resolve(it) ?: it }
        } catch (_: IllegalArgumentException) {
            null
        }

        override fun getRelativePathString(path: Path) = "$type/${super.getRelativePathString(path)}"
    }
}

