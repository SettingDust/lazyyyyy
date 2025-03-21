package settingdust.lazyyyyy.forge.xplat

import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.LoadingModList
import settingdust.lazyyyyy.PlatformService
import java.nio.file.Path

class PlatformServiceImpl : PlatformService {
    override val configDir: Path = FMLPaths.CONFIGDIR.get()

    override fun isModLoaded(modId: String) = LoadingModList.get().getModFileById(modId) != null
}