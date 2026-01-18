package settingdust.lazyyyyy.fabric.util

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.lenni0451.reflect.stream.RStream
import settingdust.lazyyyyy.util.LoaderAdapter
import java.nio.file.Path

class LoaderAdapter : LoaderAdapter {
    private val isClient = FabricLoader.getInstance().environmentType === EnvType.CLIENT
    override fun isClient() = isClient

    override fun isModLoaded(modId: String) = FabricLoader.getInstance().isModLoaded(modId)

    override fun getConfigDirectory() = FabricLoader.getInstance().configDir

    private val _modsDirectory: Path by lazy {
        RStream.of(FabricLoaderImpl::class.java).methods().by("getModsDirectory0")
            .invokeInstance(FabricLoader.getInstance())
    }

    override fun getModsDirectory() = _modsDirectory
}