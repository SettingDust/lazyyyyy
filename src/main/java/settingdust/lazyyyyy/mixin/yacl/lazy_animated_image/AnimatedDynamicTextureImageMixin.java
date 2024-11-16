package settingdust.lazyyyyy.mixin.yacl.lazy_animated_image;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import dev.isxander.yacl3.gui.image.ImageRendererFactory;
import dev.isxander.yacl3.gui.image.impl.AnimatedDynamicTextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.YaclLazyAnimatedImageKt;

import java.io.InputStream;

@IfModLoaded("yet_another_config_lib_v3")
@Mixin(AnimatedDynamicTextureImage.class)
public class AnimatedDynamicTextureImageMixin {
    @Redirect(
        method = "lambda$createWEBPFromTexture$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/Resource;open()Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$removeOpenResource(final Resource instance) {return null;}

    @WrapOperation(
        method = "lambda$createWEBPFromTexture$2",
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createWEBPSupplier(Ljava/io/InputStream;Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory$ImageSupplier;"
        )
    )
    private static ImageRendererFactory.ImageSupplier lazyyyyy$asyncPrepare(
        final InputStream ignored,
        final ResourceLocation id,
        final Operation<ImageRendererFactory.ImageSupplier> original,
        @Local Resource resource
    ) {
        return YaclLazyAnimatedImageKt.asyncPrepareWEBP(resource, (is) -> original.call(is, id));
    }

    @WrapMethod(method = "lambda$createFromImageReader$9")
    private static ImageRenderer lazyyyyy$syncComplete(
        final NativeImage image,
        final int frameWidth,
        final int frameHeight,
        final int frameCount,
        final double[] frameDelays,
        final int cols,
        final int rows,
        final ResourceLocation uniqueLocation,
        final Operation<ImageRenderer> original
    ) {
        return YaclLazyAnimatedImageKt.syncCompleteWEBP(() -> original.call(
            image,
            frameWidth,
            frameHeight,
            frameCount,
            frameDelays,
            cols,
            rows,
            uniqueLocation
        ));
    }
}
