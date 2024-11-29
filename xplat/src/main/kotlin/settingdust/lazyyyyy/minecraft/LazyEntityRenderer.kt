package settingdust.lazyyyyy.minecraft

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.collect
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.mixin.lazy_entity_renderers.EntityRendererAccessor
import java.util.function.BiConsumer

private val emptyContext = EntityRendererProvider.Context(null, null, null, null, null, null, null)

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

class LazyEntityRenderer<T : Entity>(
    val type: EntityType<T>,
    val context: EntityRendererProvider.Context,
    val wrapped: () -> EntityRenderer<T>
) : EntityRenderer<T>(emptyContext) {
    companion object {
        val onAddLayer =
            MutableSharedFlow<Triple<EntityType<*>, EntityRendererProvider.Context, LivingEntityRenderer<*, *>>>()
    }

    private val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { wrapped() }
    val renderer by lazy {
        runBlocking {
            val renderer = loading.await()
            if (renderer is LivingEntityRenderer<*, *>) {
                onAddLayer.emit(Triple(type, context, renderer))
            }
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

    override fun shouldShowName(entity: T) =
        (renderer as EntityRendererAccessor<T>).invokeShouldShowName(entity)

    override fun getTextureLocation(entity: T) = renderer.getTextureLocation(entity)

    override fun getFont() = renderer.font

    override fun renderNameTag(
        entity: T,
        component: Component,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) = (renderer as EntityRendererAccessor<T>).invokeRenderNameTag(
        entity,
        component,
        poseStack,
        multiBufferSource,
        i
    )
}

class LazyBlockEntityRenderer<T : BlockEntity>(
    val context: BlockEntityRendererProvider.Context,
    val wrapped: () -> BlockEntityRenderer<T>
) : BlockEntityRenderer<T> {
    private val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { wrapped() }
    val renderer by lazy { runBlocking { loading.await() } }

    override fun render(
        blockEntity: T,
        f: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int,
        j: Int
    ) = renderer.render(blockEntity, f, poseStack, multiBufferSource, i, j)

    override fun shouldRenderOffScreen(blockEntity: T) = renderer.shouldRenderOffScreen(blockEntity)

    override fun getViewDistance() = renderer.viewDistance

    override fun shouldRender(blockEntity: T, vec3: Vec3) = renderer.shouldRender(blockEntity, vec3)
}