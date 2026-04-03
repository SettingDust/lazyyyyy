package settingdust.lazyyyyy.neoforge

import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.util.Entrypoint
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(Lazyyyyy.ID)
object LazyyyyyNeoForge {
    init {
        requireNotNull(Lazyyyyy)
        Entrypoint.construct()
        MOD_BUS.apply {
            addListener<FMLCommonSetupEvent> {
                Entrypoint.init()
            }
            addListener<FMLClientSetupEvent> { Entrypoint.clientInit() }
        }
    }
}