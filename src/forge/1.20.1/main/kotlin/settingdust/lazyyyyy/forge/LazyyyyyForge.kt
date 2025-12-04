package settingdust.lazyyyyy.forge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.util.Entrypoint
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Lazyyyyy.ID)
object LazyyyyyForge {
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