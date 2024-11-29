package settingdust.lazyyyyy.forge

import kotlinx.coroutines.launch
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.fml.common.Mod
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer

@Mod(Lazyyyyy.ID)
object LazyyyyyForge {
    init {
        Lazyyyyy.scope.launch {
            launch {
                LazyEntityRenderer.onAddLayer.collect { (type, context, renderer) ->
                    ModLoader.get()
                        .postEvent(EntityRenderersEvent.AddLayers(mapOf(type to renderer), emptyMap(), context))
                }
            }

            launch {
                LazyPlayerRenderer.onAddLayer.collect { (type, context, renderer) ->
                    ModLoader.get()
                        .postEvent(EntityRenderersEvent.AddLayers(emptyMap(), mapOf(type to renderer), context))
                }
            }
        }

        Lazyyyyy.init()
    }
}