package settingdust.lazyyyyy.mixin.lazy_entity_renderers.spectrum;

import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.blocks.mob_head.SpectrumSkullType;
import de.dafuqs.spectrum.blocks.mob_head.client.SpectrumSkullBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.minecraft.LazyEntityRenderersKt;

@Mixin(SpectrumSkullBlockEntityRenderer.class)
public class SpectrumSkullBlockEntityRendererMixin {
    @Inject(method = "getRenderLayer", remap = false, at = @At("HEAD"))
    private static void lazyyyyy$initTheRenderer(
        final SpectrumSkullType type,
        final CallbackInfoReturnable<RenderType> cir
    ) {
        // FIXME architectury transform production has problem. So, we can't make reference from fabric mod working with xplat lexforge module
        // Need to replace with SpectrumBlockEntities.SKULL
        LazyEntityRenderersKt.getRenderers(Minecraft.getInstance().getBlockEntityRenderDispatcher())
                             .get(BuiltInRegistries.BLOCK_ENTITY_TYPE
                                 .get(new ResourceLocation(SpectrumCommon.MOD_ID, "skull")));
    }
}
