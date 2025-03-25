package settingdust.lazyyyyy.forge.minecraft

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.fml.ModLoader
import settingdust.lazyyyyy.minecraft.DummyLivingEntityRenderer
import settingdust.lazyyyyy.minecraft.DummyPlayerRenderer
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer
import settingdust.lazyyyyy.minecraft.ObservableMap
import settingdust.lazyyyyy.minecraft.`lazyyyyy$playerRenderers`
import settingdust.lazyyyyy.minecraft.`lazyyyyy$renderers`
import settingdust.lazyyyyy.minecraft.playerRenderers

object LazyEntityRenderersForge {
    init {
        CoroutineScope(Dispatchers.Default).launch {
            launch {
                LazyEntityRenderer.onLoaded.collect { (type, context, renderer) ->
                    val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
                    entityRenderDispatcher.`lazyyyyy$renderers`[type] = renderer
                    if (renderer !is LivingEntityRenderer<*, *>) return@collect
                    val originalRenderers = entityRenderDispatcher.renderers
                    entityRenderDispatcher.renderers = mapOf(type to renderer)
                    ModLoader.get()
                        .postEvent(
                            EntityRenderersEvent.AddLayers(
                                entityRenderDispatcher.renderers,
                                emptyMap(),
                                context
                            )
                        )
                    entityRenderDispatcher.renderers = originalRenderers
                }
            }

            launch {
                LazyPlayerRenderer.onLoaded.collect { (type, context, renderer) ->
                    val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
                    entityRenderDispatcher.`lazyyyyy$playerRenderers`[type] = renderer
                    val originalRenderers = entityRenderDispatcher.playerRenderers
                    entityRenderDispatcher.playerRenderers = mapOf(type to renderer)
                    ModLoader.get()
                        .postEvent(
                            EntityRenderersEvent.AddLayers(
                                emptyMap(),
                                entityRenderDispatcher.playerRenderers,
                                context
                            )
                        )
                    entityRenderDispatcher.playerRenderers = originalRenderers
                }
            }
        }
    }
}

fun Map<EntityType<*>, EntityRenderer<*>>.filterLazyRenderers() =
    filterTo(hashMapOf()) { it.value !is LazyEntityRenderer }

/**
 * Not using since many mods are casting the renderer if exists. Need to use mixin to compat with them.
 * Leave the renderer to null and fix the mods no checking is easier.
 */
fun Map<EntityType<*>, EntityRenderer<*>>.replaceWithDummyLivingEntity(context: EntityRendererProvider.Context) =
    mapValues { (_, renderer) -> if (renderer is LazyEntityRenderer) DummyLivingEntityRenderer(context) else renderer }

/**
 * Some mods are using the player renderer without condition
 * https://github.com/WayofTime/BloodMagic/blob/1.20.1/src/main/java/wayoftime/bloodmagic/client/ClientEvents.java#L250-L256
 */
fun Map<String, EntityRenderer<out Player>>.replaceWithDummyPlayer(context: EntityRendererProvider.Context) =
    mapValues { (_, renderer) -> if (renderer is LazyPlayerRenderer) DummyPlayerRenderer(context) else renderer }

fun Map<EntityType<*>, EntityRenderer<*>>.observeEntityRenderers() = ObservableMap(this) {
    val renderer = Minecraft.getInstance().entityRenderDispatcher.`lazyyyyy$renderers`[it]
    if (renderer is LazyEntityRenderer<*>) runBlocking { renderer.loading.await() } else renderer
}

fun Map<String, EntityRenderer<out Player>>.observePlayerRenderers() = ObservableMap(this) {
    val renderer = Minecraft.getInstance().entityRenderDispatcher.`lazyyyyy$playerRenderers`[it]
    if (renderer is LazyPlayerRenderer) runBlocking { renderer.loading.await() } else renderer
}