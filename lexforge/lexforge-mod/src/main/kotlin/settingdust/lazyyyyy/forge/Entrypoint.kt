package settingdust.lazyyyyy.forge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.fml.common.Mod
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer
import settingdust.lazyyyyy.minecraft.playerRenderers

@Mod(Lazyyyyy.ID)
object LazyyyyyForge {
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            launch {
                LazyEntityRenderer.onAddLayer.collect { (type, context, renderer) ->
                    val originalRenderers = Minecraft.getInstance().entityRenderDispatcher.renderers
                    Minecraft.getInstance().entityRenderDispatcher.renderers = mutableMapOf(type to renderer)
                    ModLoader.get()
                        .postEvent(
                            EntityRenderersEvent.AddLayers(
                                Minecraft.getInstance().entityRenderDispatcher.renderers,
                                emptyMap(),
                                context
                            )
                        )
                    Minecraft.getInstance().entityRenderDispatcher.renderers = originalRenderers
                }
            }

            launch {
                LazyPlayerRenderer.onAddLayer.collect { (type, context, renderer) ->
                    val originalRenderers = Minecraft.getInstance().entityRenderDispatcher.playerRenderers
                    Minecraft.getInstance().entityRenderDispatcher.playerRenderers = mutableMapOf(type to renderer)
                    ModLoader.get()
                        .postEvent(
                            EntityRenderersEvent.AddLayers(
                                emptyMap(),
                                Minecraft.getInstance().entityRenderDispatcher.playerRenderers,
                                context
                            )
                        )
                    Minecraft.getInstance().entityRenderDispatcher.playerRenderers = originalRenderers
                }
            }
        }

        Lazyyyyy.init()
    }
}