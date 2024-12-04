package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.big_brain;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tallestegg.bigbrain.client.BigBrainRendererEvents;
import tallestegg.bigbrain.client.renderers.layers.DrownedGlowLayer;

@IfModLoaded("bigbrain")
@Mixin(BigBrainRendererEvents.class)
public class BigBrainRendererEventsMixin {
    @WrapWithCondition(
        method = "addRenderLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/DrownedRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$cancelAddIfNull(final DrownedRenderer instance, final RenderLayer renderLayer) {
        return instance != null;
    }

    @WrapOperation(
        method = "addRenderLayers",
        at = @At(value = "NEW", target = "tallestegg/bigbrain/client/renderers/layers/DrownedGlowLayer")
    )
    private static DrownedGlowLayer<?, ?> lazyyyyy$cancelNewIfNull(
        final RenderLayerParent<?, ?> p_117507_,
        final Operation<DrownedGlowLayer<?, ?>> original
    ) {
        if (p_117507_ == null) return null;
        return original.call(p_117507_);
    }
}
