package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.faster_mixin.cache.generator.LocalRefClassGeneratorCache;

@Mixin(MixinExtrasBootstrap.class)
public class MixinExtrasBootstrapTransformer {
    @Inject(method = "init", at = @At("TAIL"))
    private static void lazyyyyy$replayLocalRefCache(CallbackInfo ci) {
        LocalRefClassGeneratorCache.replay();
    }
}

