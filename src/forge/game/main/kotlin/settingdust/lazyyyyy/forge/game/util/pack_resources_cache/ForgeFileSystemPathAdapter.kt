package settingdust.lazyyyyy.forge.game.util.pack_resources_cache

import cpw.mods.niofs.union.UnionFileSystem
import settingdust.lazyyyyy.game.util.pack_resources_cache.DefaultFileSystemPathAdapter
import settingdust.lazyyyyy.game.util.pack_resources_cache.FileSystemPathAdapter
import java.nio.file.Files
import java.nio.file.Path

class ForgeFileSystemPathAdapter : FileSystemPathAdapter {
    override fun getFileSystemPath(path: Path): Path? {
        // First try the default zip file system handling
        val result = DefaultFileSystemPathAdapter.getFileSystemPath(path)
        if (result != null) return result

        // Then check for Forge's UnionFileSystem
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
