package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.joy_of_painting;

import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.Entities;

import java.util.Map;

@IfModLoaded(XercaPaint.MODID)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$initTheRenderer(
        final ResourceManager resourceManager,
        final CallbackInfo ci,
        @Local EntityRendererProvider.Context context
    ) {
        renderers.get(Entities.CANVAS.get());
    }
}