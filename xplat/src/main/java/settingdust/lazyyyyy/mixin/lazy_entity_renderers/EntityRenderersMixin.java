package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer;
import settingdust.lazyyyyy.minecraft.LazyEntityRendererKt;
import settingdust.lazyyyyy.minecraft.LazyPlayerRenderer;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    /**
     * Meanness since the renderers are lazy
     */
    // @Redirect(
    //     method = "createEntityRenderers",
    //     at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")
    // )
    private static void lazyyyyy$asyncCreateRenderer(
        final Map<EntityType<?>, EntityRendererProvider<?>> instance,
        final BiConsumer<EntityType<?>, EntityRendererProvider<?>> consumer,
        EntityRendererProvider.Context context,
        @Local ImmutableMap.Builder<EntityType<?>, EntityRenderer<?>> builder
    ) {
        LazyEntityRendererKt.createEntityRenderersAsync(instance, consumer);
    }

    @WrapOperation(
        method = {"method_32174", "m_257087_"},
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider;create(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Lnet/minecraft/client/renderer/entity/EntityRenderer;"
        )
    )
    private static <T extends Entity> EntityRenderer<T> lazyyyyy$createEntityRenderers$lazyCreateRenderer(
        final EntityRendererProvider<T> instance,
        final EntityRendererProvider.Context context,
        final Operation<EntityRenderer<T>> original,
        @Local(argsOnly = true) EntityType<T> type
    ) {
        return new LazyEntityRenderer<>(type, context, () -> original.call(instance, context));
    }

    @WrapOperation(
        method = {"method_32175", "m_234604_"},
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider;create(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Lnet/minecraft/client/renderer/entity/EntityRenderer;"
        )
    )
    private static EntityRenderer<Player> lazyyyyy$createPlayerRenderers$lazyCreateRenderer(
        final EntityRendererProvider<Player> instance,
        final EntityRendererProvider.Context context,
        final Operation<EntityRenderer<Player>> original,
        @Local(argsOnly = true) String skin
    ) {
        return new LazyPlayerRenderer(skin, context, () -> original.call(instance, context));
    }
}
