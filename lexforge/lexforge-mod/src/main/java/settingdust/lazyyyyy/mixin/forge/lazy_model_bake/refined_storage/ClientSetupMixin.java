package settingdust.lazyyyyy.mixin.forge.lazy_model_bake.refined_storage;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.refinedmods.refinedstorage.render.BakedModelOverrideRegistry;
import com.refinedmods.refinedstorage.setup.ClientSetup;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.modernfix.ModernFix;
import org.embeddedt.modernfix.ModernFixClient;
import org.embeddedt.modernfix.api.entrypoint.ModernFixClientIntegration;
import org.embeddedt.modernfix.dynamicresources.DynamicBakedModelProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

//@IfModLoaded(RS.ID)
@IfModLoaded(ModernFix.MODID)
@Mixin(ClientSetup.class)
public class ClientSetupMixin implements ModernFixClientIntegration {
    @Shadow(remap = false)
    @Final
    private static BakedModelOverrideRegistry BAKED_MODEL_OVERRIDE_REGISTRY;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void lazyyyyy$init(final CallbackInfo ci) {
        ModernFixClient.CLIENT_INTEGRATIONS.add(this);
    }

    @Redirect(
        method = "onModelBake",
        remap = false,
        at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;")
    )
    private static Set lazyyyyy$disableLoop(final Map instance) {
        return Collections.emptySet();
    }

    @Override
    public BakedModel onBakedModelLoad(
        final ResourceLocation location,
        final UnbakedModel baseModel,
        final BakedModel originalModel,
        final ModelState state,
        final ModelBakery bakery,
        final Function<Material, TextureAtlasSprite> textureGetter
    ) {
        var id = new ResourceLocation(location.getNamespace(), location.getPath());
        var factory = BAKED_MODEL_OVERRIDE_REGISTRY.get(id);
        if (factory == null) return originalModel;

        return factory.create(originalModel, DynamicBakedModelProvider.currentInstance);
    }
}
