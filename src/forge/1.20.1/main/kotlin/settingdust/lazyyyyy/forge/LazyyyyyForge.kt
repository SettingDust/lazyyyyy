package settingdust.lazyyyyy.forge

import dev.nyon.klf.MOD_BUS
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.util.Entrypoint

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