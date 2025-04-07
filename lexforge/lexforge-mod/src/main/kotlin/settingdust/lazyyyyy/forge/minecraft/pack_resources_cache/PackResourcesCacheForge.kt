package settingdust.lazyyyyy.forge.minecraft.pack_resources_cache

import cpw.mods.niofs.union.UnionFileSystem
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

fun getFilePath(root: Path): Path? {
    val fs: FileSystem = root.getFileSystem()
    if (fs is UnionFileSystem && Files.isRegularFile(fs.primaryPath)) {
        return fs.primaryPath
    }
    return null
}