package settingdust.lazyyyyy.puzzlelib

import fuzs.puzzleslib.api.client.event.v1.ModelEvents
import fuzs.puzzleslib.api.event.v1.core.EventInvoker
import fuzs.puzzleslib.api.event.v1.core.EventPhase
import fuzs.puzzleslib.impl.event.core.EventInvokerImpl
import fuzs.puzzleslib.impl.event.core.EventInvokerImpl.EventInvokerLike
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier
import net.minecraft.client.resources.model.BlockModelRotation
import net.minecraft.client.resources.model.ModelBakery
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation

data class ImmediateEventInvoker<T>(val invoker: (T) -> Unit) : EventInvoker<T>, EventInvokerLike<T> {
    override fun register(phase: EventPhase, callback: T) {
        invoker.invoke(callback)
    }

    override fun asEventInvoker(context: Any?) = EventInvoker<T> { phase: EventPhase, callback: T ->
        register(EventPhase.DEFAULT, callback)
    }
}

fun fabricApiModifyUnbakedModelEventInvoker() =
    EventInvokerImpl.register(ModelEvents.ModifyUnbakedModel::class.java, ImmediateEventInvoker { callback ->
        ModelLoadingPlugin.register { context ->
            val models = mutableMapOf<ResourceLocation, UnbakedModel>()
            context.modifyModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE) { model, context ->
                val result = callback.onModifyUnbakedModel(
                    context.id(),
                    { model },
                    { context.loader().getModel(it) }
                ) { id, model ->
                    require(id !is ModelResourceLocation) {
                        "model resource location is not supported"
                    }
                    models[id] = model
                }
                return@register result.interrupt.orElse(model)
            }
            context.resolveModel().register { context -> models[context.id()] }
        }
    }, false)

fun fabricApiModifyBakedModelEventInvoker() =
    EventInvokerImpl.register(ModelEvents.ModifyBakedModel::class.java, ImmediateEventInvoker { callback ->
        ModelLoadingPlugin.register { context ->
            context.modifyModelAfterBake().register(ModelModifier.OVERRIDE_PHASE) { model, context ->
                val model = model ?: return@register null
                val models = context.loader().bakedTopLevelModels
                val result = callback.onModifyBakedModel(
                    context.id(),
                    { model },
                    { context.baker() },
                    { id -> models[id] ?: context.baker().bake(id, BlockModelRotation.X0_Y0) },
                    { id, model -> models.putIfAbsent(id, model) })
                return@register result.interrupt.orElse(model)
            }
        }
    }, false)

fun fabricApiAdditionalBakedModelEventInvoker() =
    EventInvokerImpl.register(ModelEvents.AdditionalBakedModel::class.java, ImmediateEventInvoker { callback ->
        ModelLoadingPlugin.register { context ->
            context.modifyModelAfterBake().register(ModelModifier.OVERRIDE_PHASE) { model, context ->
                if (context.id() != ModelBakery.MISSING_MODEL_LOCATION) return@register model
                val models = context.loader().bakedTopLevelModels
                callback.onAdditionalBakedModel(
                    { id, model -> models.putIfAbsent(id, model) },
                    { id -> models[id] ?: context.baker().bake(id, BlockModelRotation.X0_Y0) },
                    { context.baker() })
                return@register model
            }
        }
    }, false)