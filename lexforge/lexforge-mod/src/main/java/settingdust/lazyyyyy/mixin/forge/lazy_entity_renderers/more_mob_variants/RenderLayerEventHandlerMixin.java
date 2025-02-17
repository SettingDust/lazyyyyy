package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.more_mob_variants;

import com.github.nyuppo.client.RenderLayerEventHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderLayerEventHandler.class)
public class RenderLayerEventHandlerMixin {
    @ModifyExpressionValue(
        method = "addLayerToRenderer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/event/EntityRenderersEvent$AddLayers;getRenderer(Lnet/minecraft/world/entity/EntityType;)Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
        )
    )
    private static LivingEntityRenderer lazyyyyy$avoidCrash(@Nullable final LivingEntityRenderer original) {
        if (!(original instanceof LivingEntityRenderer)) return null;
        return original;
    }
}
