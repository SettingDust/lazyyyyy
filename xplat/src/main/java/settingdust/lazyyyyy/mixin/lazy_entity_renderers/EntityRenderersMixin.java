package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.LazyLivingEntityRendererKt;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    @Redirect(
        method = "createEntityRenderers",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")
    )
    private static void lazyyyyy$asyncCreateRenderer(
        final Map<EntityType<?>, EntityRendererProvider<?>> instance,
        final BiConsumer<EntityType<?>, EntityRendererProvider<?>> consumer,
        EntityRendererProvider.Context context,
        @Local ImmutableMap.Builder<EntityType<?>, EntityRenderer<?>> builder
    ) {
        LazyLivingEntityRendererKt.createEntityRenderersAsync(instance, consumer);
    }
}
