package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import settingdust.lazyyyyy.faster_mixin.cache.MixinConfigAccessor;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinConfig")
public abstract class MixinConfigTransformer implements MixinConfigAccessor {
    @Shadow
    public abstract boolean hasMixinsFor(String className);
}
