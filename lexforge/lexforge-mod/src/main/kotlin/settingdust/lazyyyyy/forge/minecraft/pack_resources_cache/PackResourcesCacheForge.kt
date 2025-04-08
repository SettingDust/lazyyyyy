package settingdust.lazyyyyy.forge.minecraft.pack_resources_cache

import cpw.mods.niofs.union.UnionFileSystem
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun getFilePath(root: Path): Path? {
    val fs: FileSystem = root.fileSystem
    if (fs is UnionFileSystem && Files.isRegularFile(fs.primaryPath)) {
        return fs.primaryPath
    }
    if (fs::class.qualifiedName == "jdk.nio.zipfs.ZipFileSystem") {
        val zipFile = Paths.get(fs.toString())
        if (Files.isRegularFile(zipFile)) {
            return zipFile
        }
    }
    return null
}