package settingdust.lazyyyyy.forge.core.faster_mixin.hack;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;

import java.lang.reflect.Field;
import java.util.Map;

public class MixinConfigReflection {
    public static final Class<Config> configClass = Config.class;
    public static final Field allConfigsField;

    public static final Class<?> mixinConfigClass;
    public static final Field pluginField;
    public static final Field refmapField;

    static {
        try {
            allConfigsField = configClass.getDeclaredField("allConfigs");
            allConfigsField.setAccessible(true);

            mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
            pluginField = mixinConfigClass.getDeclaredField("pluginClassName");
            pluginField.setAccessible(true);
            refmapField = mixinConfigClass.getDeclaredField("refMapperConfig");
            refmapField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Config> getAllConfigs() {
        try {
            return (Map<String, Config>) allConfigsField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPlugin(IMixinConfig config) {
        try {
            return (String) pluginField.get(config);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRefmap(IMixinConfig config) {
        try {
            return (String) refmapField.get(config);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setRefmap(IMixinConfig config, String refmap) {
        try {
            refmapField.set(config, refmap);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
