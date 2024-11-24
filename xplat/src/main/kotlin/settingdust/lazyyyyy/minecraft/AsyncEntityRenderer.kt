package settingdust.lazyyyyy.minecraft

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.mixin.async_entity_renderers.EntityRendererAccessor
import settingdust.lazyyyyy.mixin.async_entity_renderers.LivingEntityRendererAccessor
import settingdust.lazyyyyy.onEachAsync
import java.util.function.BiConsumer

private val emptyContext = EntityRendererProvider.Context(null, null, null, null, null, null, null)

fun createEntityRenderersAsync(
    providers: Map<EntityType<*>, EntityRendererProvider<*>>,
    consumer: BiConsumer<EntityType<*>, EntityRendererProvider<*>>
) = runBlocking {
    providers.entries.asFlow().onEachAsync {
        consumer.accept(it.key, it.value)
    }.launchIn(Lazyyyyy.scope).join()
}

/**
 * The both classes below won't work since we can't know the type provided by the registered provider
 */
class LazyEntityRenderer<T : Entity>(
    val context: EntityRendererProvider.Context,
    val wrapped: EntityRendererProvider<T>
) : EntityRenderer<T>(emptyContext) {
    val loadingRenderer = Lazyyyyy.scope.async { wrapped.create(context) }
    val renderer by lazy { runBlocking { loadingRenderer.await() } }

    override fun getSkyLightLevel(entity: T, blockPos: BlockPos) =
        (renderer as EntityRendererAccessor<T>).invokeGetSkyLightLevel(entity, blockPos)

    override fun getBlockLightLevel(entity: T, blockPos: BlockPos) =
        (renderer as EntityRendererAccessor<T>).invokeGetBlockLightLevel(entity, blockPos)

    override fun shouldRender(entity: T, frustum: Frustum, d: Double, e: Double, f: Double) =
        renderer.shouldRender(entity, frustum, d, e, f)

    override fun getRenderOffset(entity: T, f: Float) = renderer.getRenderOffset(entity, f)

    override fun render(
        entity: T,
        f: Float,
        g: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = renderer.render(entity, f, g, poseStack, multiBufferSource, i)

    override fun shouldShowName(entity: T) = (renderer as EntityRendererAccessor<T>).invokeShouldShowName(entity)

    override fun getTextureLocation(entity: T) = renderer.getTextureLocation(entity)

    override fun getFont() = renderer.font

    override fun renderNameTag(
        entity: T,
        component: Component,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = (renderer as EntityRendererAccessor<T>).invokeRenderNameTag(entity, component, poseStack, multiBufferSource, i)
}

class LazyLivingEntityRenderer<T : LivingEntity, M : EntityModel<T>>(
    val context: EntityRendererProvider.Context,
    val wrapped: EntityRendererProvider<T>
) : LivingEntityRenderer<T, M>(emptyContext, null, 0f) {
    private val loadingRenderer = Lazyyyyy.scope.async { wrapped.create(context) }
    val renderer: LivingEntityRenderer<T, M> by lazy {
        runBlocking {
            val renderer = loadingRenderer.await() as? LivingEntityRenderer<T, M>
            (renderer as LivingEntityRendererAccessor<T, M>).layers.addAll(layers)
            renderer
        }
    }

    override fun getPackedLightCoords(entity: T, f: Float) = renderer.getPackedLightCoords(entity, f)

    override fun getSkyLightLevel(entity: T, blockPos: BlockPos) =
        (renderer as EntityRendererAccessor<T>).invokeGetSkyLightLevel(entity, blockPos)

    override fun getBlockLightLevel(entity: T, blockPos: BlockPos) =
        (renderer as EntityRendererAccessor<T>).invokeGetBlockLightLevel(entity, blockPos)

    override fun shouldRender(entity: T, frustum: Frustum, d: Double, e: Double, f: Double) =
        renderer.shouldRender(entity, frustum, d, e, f)

    override fun getRenderOffset(entity: T, f: Float) = renderer.getRenderOffset(entity, f)

    override fun render(
        entity: T,
        f: Float,
        g: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = renderer.render(entity, f, g, poseStack, multiBufferSource, i)

    override fun shouldShowName(entity: T) = (renderer as EntityRendererAccessor<T>).invokeShouldShowName(entity)

    override fun getTextureLocation(entity: T) = renderer.getTextureLocation(entity)

    override fun getFont() = renderer.font

    override fun renderNameTag(
        entity: T,
        component: Component,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = (renderer as EntityRendererAccessor<T>).invokeRenderNameTag(entity, component, poseStack, multiBufferSource, i)
}