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
import settingdust.lazyyyyy.yacl.YaclLazyAnimatedImageKt;

import java.io.InputStream;
import java.nio.file.Path;

@IfModLoaded("yet_another_config_lib_v3")
@Mixin(value = AnimatedDynamicTextureImage.class)
public class AnimatedDynamicTextureImageMixin {
    @Redirect(
        method = {"lambda$createWEBPFromTexture$2", "lambda$createGIFFromTexture$0"},
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/server/packs/resources/Resource;open()Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$removeOpenResource(final Resource instance) {return null;}

    @WrapOperation(
        method = "lambda$createWEBPFromTexture$2",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createWEBPSupplier(Ljava/io/InputStream;Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory$ImageSupplier;"
        )
    )
    private static ImageRendererFactory.ImageSupplier lazyyyyy$createWEBPFromTexture$asyncPrepare(
        final InputStream ignored,
        final ResourceLocation id,
        final Operation<ImageRendererFactory.ImageSupplier> original,
        @Local Resource resource
    ) {
        return YaclLazyAnimatedImageKt.asyncPrepareImageFromTexture(resource, (is) -> original.call(is, id));
    }

    @WrapOperation(
        method = "lambda$createWEBPFromPath$3",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createWEBPSupplier(Ljava/io/InputStream;Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory$ImageSupplier;"
        )
    )
    private static ImageRendererFactory.ImageSupplier lazyyyyy$createWEBPFromPath$asyncPrepare(
        final InputStream ignored,
        final ResourceLocation id,
        final Operation<ImageRendererFactory.ImageSupplier> original,
        Path path
    ) {
        return YaclLazyAnimatedImageKt.asyncPrepareImageFromPath(path, (is) -> original.call(is, id));
    }


    @WrapOperation(
        method = "lambda$createGIFFromTexture$0",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createGIFSupplier(Ljava/io/InputStream;Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory$ImageSupplier;"
        )
    )
    private static ImageRendererFactory.ImageSupplier lazyyyyy$createGIFFromTexture$asyncPrepare(
        final InputStream ignored,
        final ResourceLocation id,
        final Operation<ImageRendererFactory.ImageSupplier> original,
        @Local Resource resource
    ) {
        return YaclLazyAnimatedImageKt.asyncPrepareImageFromTexture(resource, (is) -> original.call(is, id));
    }

    @WrapOperation(
        method = "lambda$createGIFFromPath$1",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ldev/isxander/yacl3/gui/image/impl/AnimatedDynamicTextureImage;createGIFSupplier(Ljava/io/InputStream;Lnet/minecraft/resources/ResourceLocation;)Ldev/isxander/yacl3/gui/image/ImageRendererFactory$ImageSupplier;"
        )
    )
    private static ImageRendererFactory.ImageSupplier lazyyyyy$createGIFFromPath$asyncPrepare(
        final InputStream ignored,
        final ResourceLocation id,
        final Operation<ImageRendererFactory.ImageSupplier> original,
        Path path
    ) {
        return YaclLazyAnimatedImageKt.asyncPrepareImageFromPath(path, (is) -> original.call(is, id));
    }


    @WrapMethod(
        method = "lambda$createFromImageReader$9",
        remap = false
    )
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
        return YaclLazyAnimatedImageKt.syncCompleteImage(() -> original.call(
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
