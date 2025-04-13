package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.tenshilib;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.forge.platform.patreon.ClientPatreonImpl;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded(TenshiLib.MODID)
@Mixin(ClientPatreonImpl.class)
public class ClientPatreonImplMixin {
    @WrapWithCondition(
        method = "lambda$setup$0",
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$avoidNull(final LivingEntityRenderer<?, ?> instance, final RenderLayer<?, ?> arg) {
        return instance != null;
    }
}
