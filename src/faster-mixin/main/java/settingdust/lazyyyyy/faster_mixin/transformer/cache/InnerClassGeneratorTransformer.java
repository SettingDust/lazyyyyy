package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.google.common.hash.Hashing;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.generator.InnerClassGeneratorCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.InnerClassGenerator")
public class InnerClassGeneratorTransformer {
    @Dynamic
    @Redirect(
            method = "registerInnerClass",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/transformer/InnerClassGenerator;getUniqueReference(Ljava/lang/String;Lorg/spongepowered/asm/mixin/transformer/ClassInfo;)Ljava/lang/String;"))
    private String lazyyyyy$stableUniqueReference(
            String originalName,
            ClassInfo targetClass,
            @CLocalVariable(name = "owner") IMixinInfo owner) throws IOException {
        var config = owner.getConfig();
        var source = config.getSource();
        var hasher = Hashing.murmur3_32_fixed().newHasher();
        hasher.putString(originalName, StandardCharsets.UTF_8);
        if (source instanceof IHashProvider provider) {
            hasher.putBytes(provider.lazyyyyy$getHash());
        }
        String name = originalName.substring(originalName.lastIndexOf('$') + 1);
        if (name.matches("^[0-9]+$")) {
            name = "Anonymous";
        }
        return String.format("%s$%s$%s", targetClass, name, hasher.hash());
    }

    @Inject(
            method = "registerInnerClass",
            at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/util/IConsumer;accept(Ljava/lang/Object;)V"))
    private void lazyyyyy$captureInfo(
            CallbackInfo ci,
            @CLocalVariable(name = "owner") IMixinInfo owner,
            @CLocalVariable(name = "targetClass") ClassInfo targetClass,
            @CLocalVariable(name = "innerClassName") String innerClassName) {
        InnerClassGeneratorCache.CAPTURED_INFOS.put(
                innerClassName,
                new InnerClassGeneratorCache.InfoData(owner.getClassName(), targetClass.getClassName()));
    }
}
