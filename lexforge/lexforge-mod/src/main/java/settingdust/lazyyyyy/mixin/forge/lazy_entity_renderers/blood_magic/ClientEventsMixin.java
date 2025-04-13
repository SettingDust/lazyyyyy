package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.blood_magic;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.ClientEvents;

@IfModLoaded(BloodMagic.MODID)
@Mixin(ClientEvents.class)
public class ClientEventsMixin {
    @WrapWithCondition(
        method = "initRenderLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$avoidNull(final PlayerRenderer instance, final RenderLayer renderLayer) {
        return instance != null;
    }
}
