package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.cloudstorage;

import com.github.alexthe668.cloudstorage.CloudStorage;
import com.github.alexthe668.cloudstorage.client.ClientProxy;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded(CloudStorage.MODID)
@Mixin(ClientProxy.class)
public class ClientProxyMixin {
    @WrapWithCondition(
        method = "onAddLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"
        )
    )
    private static boolean lazyyyyy$avoidNull(final LivingEntityRenderer instance, final RenderLayer arg) {
        return instance != null;
    }
}
