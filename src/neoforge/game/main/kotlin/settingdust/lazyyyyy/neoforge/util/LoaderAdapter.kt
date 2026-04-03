package settingdust.lazyyyyy.neoforge.util

import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.LoadingModList
import settingdust.lazyyyyy.util.LoaderAdapter

class LoaderAdapter : LoaderAdapter {
    override val isClient: Boolean
        get() = FMLLoader.getDist().isClient

    override fun isModLoaded(modId: String) = LoadingModList.get().getModFileById(modId) != null
}