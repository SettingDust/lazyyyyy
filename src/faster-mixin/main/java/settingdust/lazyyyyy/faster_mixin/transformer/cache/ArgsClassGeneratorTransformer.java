package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.google.common.hash.Hashing;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.service.ISyntheticClassInfo;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.generator.ArgsClassGeneratorCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Mixin(targets = "org.spongepowered.asm.mixin.injection.invoke.arg.ArgsClassGenerator")
public class ArgsClassGeneratorTransformer {
    @Redirect(
            method = "getArgsClass",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    private String lazyyyyy$stableName(
            String format,
            Object[] args,
            @CLocalVariable(name = "desc") String desc,
            @CLocalVariable(name = "mixin") IMixinInfo mixin) throws IOException {
        var config = mixin.getConfig();
        var source = config.getSource();
        var hasher = Hashing.murmur3_32_fixed().newHasher();
        hasher.putString(mixin.getClassName(), StandardCharsets.UTF_8);
        hasher.putString(desc, StandardCharsets.UTF_8);
        if (source instanceof IHashProvider provider) {
            hasher.putBytes(provider.lazyyyyy$getHash());
        }
        return "org.spongepowered.asm.synthetic.args.Args$" + hasher.hash();
    }

    @Inject(
            method = "getArgsClass",
            at = @At(value = "INVOKE", target = "Lorg/spongepowered/asm/util/IConsumer;accept(Ljava/lang/Object;)V"))
    private void lazyyyyy$captureInfo(String desc, IMixinInfo mixin, CallbackInfoReturnable<ISyntheticClassInfo> cir) {
        ArgsClassGeneratorCache.CAPTURED_INFOS.put(desc, mixin.getClassName());
    }
}
