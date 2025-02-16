package settingdust.lazyyyyy.forge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.forge.minecraft.LazyEntityRenderersForge

@Mod(Lazyyyyy.ID)
object LazyyyyyForge {
    init {
        Lazyyyyy.init()
        DistExecutor.safeRunWhenOn(Dist.CLIENT) {
            DistExecutor.SafeRunnable {
                LazyEntityRenderersForge
            }
        }
    }
}