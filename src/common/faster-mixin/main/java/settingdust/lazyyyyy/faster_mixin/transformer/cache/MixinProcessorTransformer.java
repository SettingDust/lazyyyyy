package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;
import settingdust.lazyyyyy.faster_mixin.cache.MixinProcessorAccessor;

import java.util.List;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinProcessor")
public abstract class MixinProcessorTransformer implements MixinProcessorAccessor {
    @Final
    @Shadow
    private List<IMixinConfig> configs;

    @Shadow
    private String sessionId;

    @Shadow
    public abstract boolean applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode);

    @Override
    @Unique
    public List<IMixinConfig> lazyyyyy$getConfigs() {
        return configs;
    }

    @Override
    public boolean lazyyyyy$applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode) {
        return applyMixins(environment, name, targetClassNode);
    }

    @Override
    @Unique
    public String lazyyyyy$getSessionId() {
        return sessionId;
    }

    @Override
    @Unique
    public void lazyyyyy$setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$loadSessionIdFromCache(CallbackInfo ci) {
        String cachedSessionId = MixinCacheManager.loadGlobalSessionId();
        if (cachedSessionId != null) {
            this.sessionId = cachedSessionId;
        } else {
            MixinCacheManager.saveGlobalSessionId(this.sessionId);
        }
    }
}
