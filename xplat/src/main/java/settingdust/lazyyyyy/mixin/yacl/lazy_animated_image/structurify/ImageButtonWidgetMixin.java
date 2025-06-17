package settingdust.lazyyyyy.mixin.yacl.lazy_animated_image.structurify;

import com.faboslav.structurify.common.config.client.gui.widget.ImageButtonWidget;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.yacl3.gui.image.ImageRendererFactory;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.yacl.AsyncImageRenderer;

@Mixin(ImageButtonWidget.class)
public class ImageButtonWidgetMixin {
    @WrapOperation(
        method = "lambda$new$0",
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createWEBPFromTexture(Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory;"
        )
    )
    private static ImageRendererFactory lazyyyyy$avoidCrash(
        final ResourceLocation textureLocation,
        final Operation<ImageRendererFactory> original
    ) {
        return () ->
            ((AsyncImageRenderer) original.call(textureLocation).prepareImage().completeImage())
                .getOriginal()
                .getValue();
    }
}
