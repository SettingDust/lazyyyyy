package settingdust.lazyyyyy.forge

import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import settingdust.lazyyyyy.minecraft.DummyPlayerRenderer
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer

fun Map<EntityType<*>, EntityRenderer<*>>.filterLazyRenderers() =
    filterTo(hashMapOf()) { it.value !is LazyEntityRenderer }

fun Map<String, EntityRenderer<out Player>>.replaceWithDummy(context: EntityRendererProvider.Context) =
    mapValues { (_, renderer) -> if (renderer is LazyPlayerRenderer) DummyPlayerRenderer(context) else renderer }