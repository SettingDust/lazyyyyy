package settingdust.lazyyyyy.forge.core.faster_mixin.injected.cache;

import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.lang.reflect.Field;

public class MixinTransformerReflection {
    public static final Class<?> mixinTransformerClass;
    public static final Field processorField;

    static {
        try {
            mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProcessor(IMixinTransformer transformer) {
        try {
            return processorField.get(transformer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
