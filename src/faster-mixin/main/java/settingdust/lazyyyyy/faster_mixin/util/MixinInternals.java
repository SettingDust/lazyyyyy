package settingdust.lazyyyyy.faster_mixin.util;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import settingdust.lazyyyyy.faster_mixin.util.accessor.MixinConfigAccessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MixinInternals {
    private static final Map<String, IMixinInfo> MIXIN_INDEX = new ConcurrentHashMap<>();
    private static final AtomicBoolean INDEX_INITIALIZED = new AtomicBoolean();
    private static final ThreadLocal<Boolean> SUPPRESS_FORNAME_WARN =
            ThreadLocal.withInitial(() -> false);

    private MixinInternals() {
    }

    public static Extensions getExtensions() {
        IMixinTransformer transformer =
                (IMixinTransformer) MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        return (Extensions) transformer.getExtensions();
    }

    public static void initMixinIndex(Iterable<IMixinConfig> configs) {
        if (!INDEX_INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        for (IMixinConfig config : configs) {
            if (config instanceof MixinConfigAccessor accessor) {
                for (IMixinInfo mixin : accessor.lazyyyyy$getMixins()) {
                    MIXIN_INDEX.putIfAbsent(mixin.getClassName(), mixin);
                }
            }
        }
    }

    public static IMixinInfo getMixin(String className) {
        return MIXIN_INDEX.get(className);
    }

    public static void setSuppressForNameWarn(boolean suppress) {
        SUPPRESS_FORNAME_WARN.set(suppress);
    }

    public static boolean isSuppressForNameWarn() {
        return SUPPRESS_FORNAME_WARN.get();
    }
}
