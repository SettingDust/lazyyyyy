package settingdust.lazyyyyy.faster_mixin.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import settingdust.lazyyyyy.faster_mixin.util.MixinInternals;
import settingdust.lazyyyyy.faster_mixin.util.accessor.ClassInfoAccessor;
import settingdust.lazyyyyy.faster_mixin.util.accessor.MixinInfoProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

@Mixin(ClassInfo.class)
public class ClassInfoTransformer implements MixinInfoProvider, ClassInfoAccessor {
    @Unique
    private static Field MIXIN_FIELD;

    @Unique
    private static Field MIXINS_FIELD;

    static {
        try {
            MIXIN_FIELD = ClassInfo.class.getDeclaredField("mixin");
            MIXINS_FIELD = ClassInfo.class.getDeclaredField("mixins");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        MIXIN_FIELD.setAccessible(true);
        MIXINS_FIELD.setAccessible(true);
    }

    @Override
    public IMixinInfo lazyyyyy$getMixin() {
        try {
            return (IMixinInfo) MIXIN_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<IMixinInfo> lazyyyyy$getMixins() {
        try {
            return (Set<IMixinInfo>) MIXINS_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Dynamic
    @Redirect(
            method = "forName",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/service/IClassBytecodeProvider;getClassNode(Ljava/lang/String;ZI)Lorg/objectweb/asm/tree/ClassNode;"))
    private static ClassNode lazyyyyy$backport(
            IClassBytecodeProvider instance,
            String name,
            boolean runTransformers,
            int readerFlags) throws IOException, ClassNotFoundException {
        return instance.getClassNode(name, runTransformers);
    }

    @Redirect(
            method = "forName",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/logging/ILogger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"))
    private static void lazyyyyy$suppressWarnForCache(
            ILogger logger,
            String message,
            Object[] params) {
        if (MixinInternals.isSuppressForNameWarn()) {
            return;
        }
        logger.warn(message, params);
    }
}
