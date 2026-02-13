package settingdust.lazyyyyy.faster_mixin.transformer.resource_provider;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.service.IMixinService;
import settingdust.lazyyyyy.faster_mixin.resource_provider.IResourceProvider;
import settingdust.lazyyyyy.faster_mixin.resource_provider.ReferenceMapperCreator;
import settingdust.lazyyyyy.faster_mixin.util.accessor.MixinConfigAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinConfig")
public abstract class MixinConfigTransformer implements MixinConfigAccessor {
    @Shadow
    private transient IMixinConfigSource source;

    @Shadow
    private transient List<IMixinInfo> mixins;

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

    @Override
    public List<IMixinInfo> lazyyyyy$getMixins() {
        return new ArrayList<>(mixins);
    }
}
