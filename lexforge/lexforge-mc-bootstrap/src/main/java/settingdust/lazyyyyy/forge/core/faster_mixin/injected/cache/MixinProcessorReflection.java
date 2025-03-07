package settingdust.lazyyyyy.forge.core.faster_mixin.injected.cache;

import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MixinProcessorReflection {
    public static final Class<?> mixinProcessorClass;
    public static final Method checkSelectMethod;
    public static final Field lockField;

    static {
        try {
            mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
            checkSelectMethod = mixinProcessorClass.getDeclaredMethod("checkSelect", MixinEnvironment.class);
            checkSelectMethod.setAccessible(true);
            lockField = mixinProcessorClass.getDeclaredField("lock");
            lockField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkSelect(Object processor, MixinEnvironment environment) {
        try {
            checkSelectMethod.invoke(processor, environment);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
