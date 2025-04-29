package settingdust.lazyyyyy.mixin.lazy_entity_renderers.botania;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.render.entity.GaiaGuardianRenderer;

@IfModLoaded(BotaniaAPI.MODID)
@Mixin(GaiaGuardianRenderer.class)
public class GaiaGuardianRendererMixin {
    @Redirect(
        method = "<init>",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lvazkii/botania/client/model/armor/ArmorModels;init(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V"
        )
    )
    private void lazyyyyy$cancelInit(final EntityRendererProvider.Context ctx) {}
}
