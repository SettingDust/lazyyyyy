package settingdust.lazyyyyy.forge

import net.minecraftforge.fml.loading.LoadingModList
import settingdust.lazyyyyy.PlatformService

class PlatformServiceImpl : PlatformService {
    override fun isModLoaded(modId: String) = LoadingModList.get().getModFileById(modId) != null
}