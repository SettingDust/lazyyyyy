package settingdust.lazyyyyy.minecraft

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.model.EntityModel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.player.PlayerRenderer
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.collect
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.mixin.lazy_entity_renderers.EntityRendererAccessor
import java.util.function.BiConsumer

fun createEntityRenderersAsync(
    providers: Map<EntityType<*>, EntityRendererProvider<*>>,
    consumer: BiConsumer<EntityType<*>, EntityRendererProvider<*>>
) = runBlocking {
    launch(Dispatchers.Default) {
        providers.entries.asFlow().concurrent().collect {
            consumer.accept(it.key, it.value)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LazyEntityRenderer<T : Entity>(
    val type: EntityType<T>,
    val context: EntityRendererProvider.Context,
    val wrapped: () -> EntityRenderer<T>
) : EntityRenderer<T>(context) {
    companion object {
        val onAddLayer =
            MutableSharedFlow<Triple<EntityType<*>, EntityRendererProvider.Context, LivingEntityRenderer<*, *>>>()
    }

    val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { wrapped() }.also { loading ->
        loading.invokeOnCompletion {
            if (it != null) return@invokeOnCompletion
            val renderer = loading.getCompleted()
            if (renderer is LivingEntityRenderer<*, *>) {
                runBlocking { onAddLayer.emit(Triple(type, context, renderer)) }
            }
        }
    }

    private fun <R> handle(loaded: EntityRenderer<T>.() -> R, loading: () -> R) =
        if (this.loading.isCompleted) {
            loaded(this.loading.getCompleted())
        } else {
            if (!this.loading.isActive) this.loading.start()
            loading()
        }

    override fun getPackedLightCoords(entity: T, f: Float) =
        handle({ getPackedLightCoords(entity, f) }, { super.getPackedLightCoords(entity, f) })

    override fun getSkyLightLevel(entity: T, blockPos: BlockPos) =
        handle(
            { (this as EntityRendererAccessor<T>).invokeGetSkyLightLevel(entity, blockPos) },
            { super.getSkyLightLevel(entity, blockPos) })

    override fun getBlockLightLevel(entity: T, blockPos: BlockPos) =
        handle(
            { (this as EntityRendererAccessor<T>).invokeGetBlockLightLevel(entity, blockPos) },
            { super.getBlockLightLevel(entity, blockPos) })

    override fun shouldRender(entity: T, frustum: Frustum, d: Double, e: Double, f: Double) =
        handle({ shouldRender(entity, frustum, d, e, f) }, { super.shouldRender(entity, frustum, d, e, f) })

    override fun getRenderOffset(entity: T, f: Float) =
        handle({ getRenderOffset(entity, f) }, { super.getRenderOffset(entity, f) })

    override fun render(
        entity: T,
        f: Float,
        g: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = handle(
        { render(entity, f, g, poseStack, multiBufferSource, i) },
        { super.render(entity, f, g, poseStack, multiBufferSource, i) })

    override fun shouldShowName(entity: T) =
        handle({ (this as EntityRendererAccessor<T>).invokeShouldShowName(entity) }, { super.shouldShowName(entity) })

    override fun getTextureLocation(entity: T) =
        handle({ getTextureLocation(entity) }, { MissingTextureAtlasSprite.getLocation() })

    override fun getFont() = handle({ font }, { super.font })

    override fun renderNameTag(
        entity: T,
        component: Component,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = handle(
        { (this as EntityRendererAccessor<T>).invokeRenderNameTag(entity, component, poseStack, multiBufferSource, i) },
        { super.renderNameTag(entity, component, poseStack, multiBufferSource, i) })
}

class DummyLivingEntityRenderer(context: EntityRendererProvider.Context) :
    LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>(context, null, 0f) {
    override fun getTextureLocation(entity: LivingEntity) = MissingTextureAtlasSprite.getLocation()
}

@OptIn(ExperimentalCoroutinesApi::class)
class LazyPlayerRenderer(
    val type: String,
    val context: EntityRendererProvider.Context,
    val wrapped: () -> EntityRenderer<AbstractClientPlayer>
) : PlayerRenderer(context, false) {
    companion object {
        val onAddLayer =
            MutableSharedFlow<Triple<String, EntityRendererProvider.Context, EntityRenderer<AbstractClientPlayer>>>()
    }

    val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { wrapped() }.also { loading ->
        loading.invokeOnCompletion {
            if (it != null) return@invokeOnCompletion
            val renderer = loading.getCompleted()
            runBlocking { onAddLayer.emit(Triple(type, context, renderer)) }
        }
    }

    private fun <R> handle(loaded: EntityRenderer<AbstractClientPlayer>.() -> R, loading: () -> R) =
        if (this.loading.isCompleted) {
            loaded(this.loading.getCompleted())
        } else {
            if (!this.loading.isActive) this.loading.start()
            loading()
        }

    override fun getPackedLightCoords(entity: AbstractClientPlayer, f: Float) =
        handle({ getPackedLightCoords(entity, f) }, { super.getPackedLightCoords(entity, f) })

    override fun getSkyLightLevel(entity: AbstractClientPlayer, blockPos: BlockPos) =
        handle(
            { (this as EntityRendererAccessor<AbstractClientPlayer>).invokeGetSkyLightLevel(entity, blockPos) },
            { super.getSkyLightLevel(entity, blockPos) })

    override fun getBlockLightLevel(entity: AbstractClientPlayer, blockPos: BlockPos) =
        handle(
            { (this as EntityRendererAccessor<AbstractClientPlayer>).invokeGetBlockLightLevel(entity, blockPos) },
            { super.getBlockLightLevel(entity, blockPos) })

    override fun shouldRender(entity: AbstractClientPlayer, frustum: Frustum, d: Double, e: Double, f: Double) =
        handle({ shouldRender(entity, frustum, d, e, f) }, { super.shouldRender(entity, frustum, d, e, f) })

    override fun getRenderOffset(entity: AbstractClientPlayer, f: Float) =
        handle({ getRenderOffset(entity, f) }, { super.getRenderOffset(entity, f) })

    override fun render(
        entity: AbstractClientPlayer,
        f: Float,
        g: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = handle(
        { render(entity, f, g, poseStack, multiBufferSource, i) },
        { super.render(entity, f, g, poseStack, multiBufferSource, i) })

    override fun shouldShowName(entity: AbstractClientPlayer) =
        handle(
            { (this as EntityRendererAccessor<AbstractClientPlayer>).invokeShouldShowName(entity) },
            { super.shouldShowName(entity) })

    override fun getTextureLocation(entity: AbstractClientPlayer) =
        handle({ getTextureLocation(entity) }, { MissingTextureAtlasSprite.getLocation() })

    override fun getFont() = handle({ font }, { super.font })

    override fun renderNameTag(
        entity: AbstractClientPlayer,
        component: Component,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = handle(
        {
            (this as EntityRendererAccessor<AbstractClientPlayer>).invokeRenderNameTag(
                entity,
                component,
                poseStack,
                multiBufferSource,
                i
            )
        },
        { super.renderNameTag(entity, component, poseStack, multiBufferSource, i) })
}

class DummyPlayerRenderer(context: EntityRendererProvider.Context) : PlayerRenderer(context, false)

class LazyBlockEntityRenderer<T : BlockEntity>(
    val context: BlockEntityRendererProvider.Context,
    val wrapped: () -> BlockEntityRenderer<T>
) : BlockEntityRenderer<T> {
    private val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { wrapped() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <R> handle(loaded: BlockEntityRenderer<T>.() -> R, loading: () -> R) =
        if (this.loading.isCompleted) {
            loaded(this.loading.getCompleted())
        } else {
            if (!this.loading.isActive) this.loading.start()
            loading()
        }

    override fun render(
        blockEntity: T,
        f: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int,
        j: Int
    ) = handle({ render(blockEntity, f, poseStack, multiBufferSource, i, j) }, { })

    override fun shouldRenderOffScreen(blockEntity: T) =
        handle({ shouldRenderOffScreen(blockEntity) }, { super.shouldRenderOffScreen(blockEntity) })

    override fun getViewDistance() = handle({ viewDistance }, { super.viewDistance })

    override fun shouldRender(blockEntity: T, vec3: Vec3) =
        handle({ shouldRender(blockEntity, vec3) }, { super.shouldRender(blockEntity, vec3) })
}