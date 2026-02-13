package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefClassGenerator;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.faster_mixin.cache.generator.LocalRefClassGeneratorCache;

@Mixin(LocalRefClassGenerator.class)
public class LocalRefClassGeneratorTransformer {
    @Inject(method = "getForType", at = @At("RETURN"))
    private static void lazyyyyy$captureInfo(Type type, CallbackInfoReturnable<String> cir) {
        if (cir.getReturnValue() != null) {
            LocalRefClassGeneratorCache.CAPTURED_INFOS.add(type.getDescriptor());
        }
    }
}
