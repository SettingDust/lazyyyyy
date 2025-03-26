package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.bovines_and_buttercups;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.merchantpug.bovinesandbuttercups.BovinesAndButtercups;
import net.merchantpug.bovinesandbuttercups.client.BovinesAndButtercupsClientForge;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded(BovinesAndButtercups.MOD_ID)
@Mixin(BovinesAndButtercupsClientForge.class)
public class BovinesAndButtercupsClientForgeMixin {
    @WrapWithCondition(
        method = "registerRenderLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/MushroomCowRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$avoidAddIfNull(final MushroomCowRenderer instance, final RenderLayer renderLayer) {
        return instance != null;
    }
}
