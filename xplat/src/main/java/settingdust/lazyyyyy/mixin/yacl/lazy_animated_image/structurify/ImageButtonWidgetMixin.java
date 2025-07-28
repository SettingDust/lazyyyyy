package settingdust.lazyyyyy.mixin.yacl.lazy_animated_image.structurify;

import com.faboslav.structurify.common.config.client.gui.widget.ImageButtonWidget;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.yacl3.gui.image.impl.AnimatedDynamicTextureImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.yacl.AsyncImageRenderer;

import java.util.concurrent.CompletableFuture;

@Mixin(ImageButtonWidget.class)
public class ImageButtonWidgetMixin {
    @ModifyExpressionValue(
        method = "<init>",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/ImageRendererManager;registerOrGetImage(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"
        )
    )
    private static CompletableFuture<AnimatedDynamicTextureImage> lazyyyyy$avoidCrash(
        final CompletableFuture<AsyncImageRenderer> original
    ) {
        return original.thenApply(it -> (AnimatedDynamicTextureImage) it.load());
    }
}
