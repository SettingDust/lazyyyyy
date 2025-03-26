package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.taniwha;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import party.lemons.taniwha.forge.TForgeClientEvents;

@IfModLoaded("taniwha")
@Mixin(TForgeClientEvents.class)
public class TForgeClientEventsMixin {
    @WrapWithCondition(
        method = "addLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$nullCheck(final LivingEntityRenderer instance, final RenderLayer arg) {
        return instance != null;
    }
}
