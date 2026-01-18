package settingdust.lazyyyyy.faster_mixin.transformer;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.service.IMixinService;
import settingdust.lazyyyyy.faster_mixin.IResourceProvider;
import settingdust.lazyyyyy.faster_mixin.ReferenceMapperCreator;

import java.io.IOException;
import java.io.InputStream;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinConfig")
public class MixinConfigTransformer {
    @Shadow
    private transient IMixinConfigSource source;

    @Redirect(
            method = "create",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/service/IMixinService;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"))
    private static InputStream lazyyyyy$getResourceFromSource(
            IMixinService instance,
            String configFile,
            @Local(argsOnly = true) IMixinConfigSource source) throws IOException {
        return IResourceProvider.getResourceAsStream(configFile, source, instance);
    }

    @Redirect(
            method = "onSelect",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/refmap/ReferenceMapper;read(Ljava/lang/String;)Lorg/spongepowered/asm/mixin/refmap/ReferenceMapper;"))
    private ReferenceMapper lazyyyyy$readFromSource(String resourcePath) {
        return ReferenceMapperCreator.read(resourcePath, source);
    }
}
