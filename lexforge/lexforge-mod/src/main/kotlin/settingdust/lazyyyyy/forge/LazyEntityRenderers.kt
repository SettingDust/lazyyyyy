package settingdust.lazyyyyy.forge

import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.world.entity.EntityType
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer

fun Map<EntityType<*>, EntityRenderer<*>>.filterLazyRenderers() = filterTo(hashMapOf()) { it.value !is LazyEntityRenderer }