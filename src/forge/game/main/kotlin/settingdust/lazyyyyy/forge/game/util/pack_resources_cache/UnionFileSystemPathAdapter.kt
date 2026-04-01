package settingdust.lazyyyyy.forge.game.util.pack_resources_cache

import cpw.mods.niofs.union.UnionFileSystem
import settingdust.lazyyyyy.game.util.pack_resources_cache.FileSystemPathAdapter
import java.nio.file.Files
import java.nio.file.Path

class UnionFileSystemPathAdapter : FileSystemPathAdapter {
    override fun getFileSystemPath(path: Path): Path? {
        val fs = path.fileSystem
        if (fs is UnionFileSystem) {
            val primaryPath = fs.primaryPath
            if (Files.isRegularFile(primaryPath)) {
                return primaryPath
            }
        }

        return null
    }
}
