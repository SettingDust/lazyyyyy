package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.curios;

import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@IfModLoaded(Curios.MODID)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$initTheArmorModels(
        final ResourceManager resourceManager,
        final CallbackInfo ci,
        @Local EntityRendererProvider.Context context
    ) {
        CuriosRendererRegistry.load();
    }
}
