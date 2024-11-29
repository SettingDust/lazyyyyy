package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.minecraft.LazyEntityRenderer;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    //    @Redirect(
    //        method = "createEntityRenderers",
    //        at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")
    //    )
    //    private static void lazyyyyy$asyncCreateRenderer(
    //        final Map<EntityType<?>, EntityRendererProvider<?>> instance,
    //        final BiConsumer<EntityType<?>, EntityRendererProvider<?>> consumer,
    //        EntityRendererProvider.Context context,
    //        @Local ImmutableMap.Builder<EntityType<?>, EntityRenderer<?>> builder
    //    ) {
    //        AsyncEntityRendererKt.createEntityRenderersAsync(instance, consumer);
    //    }

    @WrapOperation(
        method = {"method_32174", "m_257087_"},
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/entity/EntityRendererProvider;create(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Lnet/minecraft/client/renderer/entity/EntityRenderer;"
        )
    )
    private static EntityRenderer lazyyyyy$lazyCreateRenderer(
        final EntityRendererProvider instance,
        final EntityRendererProvider.Context context,
        final Operation<EntityRenderer> original,
        @Local(argsOnly = true) EntityType type
    ) {
        return new LazyEntityRenderer(type, context, () -> original.call(instance, context));
    }
}
