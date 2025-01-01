package settingdust.lazyyyyy.forge

import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import settingdust.lazyyyyy.minecraft.DummyLivingEntityRenderer
import settingdust.lazyyyyy.minecraft.DummyPlayerRenderer
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer

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

