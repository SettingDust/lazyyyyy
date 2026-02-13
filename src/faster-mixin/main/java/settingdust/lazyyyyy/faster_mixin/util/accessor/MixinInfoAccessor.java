package settingdust.lazyyyyy.faster_mixin.util.accessor;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinInfoAccessor {
    public static final Class<? extends IMixinInfo> CLASS;

    static {
        try {
            CLASS = (Class<? extends IMixinInfo>) Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
