package settingdust.lazyyyyy.forge

import cpw.mods.niofs.union.UnionFileSystem
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.LoadingModList
import settingdust.lazyyyyy.PlatformService
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

class PlatformServiceImpl : PlatformService {
    override val configDir: Path = FMLPaths.CONFIGDIR.get()

    override fun isModLoaded(modId: String) = LoadingModList.get().getModFileById(modId) != null

    override fun hasEarlyError() = LoadingModList.get().errors.isNotEmpty()

    override fun getFileSystemPath(path: Path): Path? = super.getFileSystemPath(path) ?: path.getUnionFileSystemPath()

    fun Path.getUnionFileSystemPath(): Path? {
        val fs: FileSystem = this.fileSystem
        if (fs is UnionFileSystem && Files.isRegularFile(fs.primaryPath)) {
            return fs.primaryPath
        }
        return null
    }
}