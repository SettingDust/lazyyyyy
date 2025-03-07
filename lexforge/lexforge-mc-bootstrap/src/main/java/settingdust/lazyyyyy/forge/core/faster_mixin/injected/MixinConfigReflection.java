package settingdust.lazyyyyy.forge.core.faster_mixin.injected;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class MixinConfigReflection {
    public static final Class<Config> configClass = Config.class;
    public static final Field allConfigsField;

    public static final Class<?> mixinConfigClass;
    public static final Field pluginField;
    public static final Field refmapField;
    public static final Method getMixinsForMethod;
    public static final Method hasMixinsForMethod;

    static {
        try {
            allConfigsField = configClass.getDeclaredField("allConfigs");
            allConfigsField.setAccessible(true);

            mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
            pluginField = mixinConfigClass.getDeclaredField("pluginClassName");
            pluginField.setAccessible(true);
            refmapField = mixinConfigClass.getDeclaredField("refMapperConfig");
            refmapField.setAccessible(true);
            getMixinsForMethod = mixinConfigClass.getDeclaredMethod("getMixinsFor", String.class);
            getMixinsForMethod.setAccessible(true);
            hasMixinsForMethod = mixinConfigClass.getDeclaredMethod("hasMixinsFor", String.class);
            hasMixinsForMethod.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
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

    public static List<IMixinInfo> getMixinsFor(IMixinConfig config, String targetClassName) {
        try {
            return (List<IMixinInfo>) getMixinsForMethod.invoke(config, targetClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasMixinsFor(IMixinConfig config, String targetClassName) {
        try {
            return (boolean) hasMixinsForMethod.invoke(config, targetClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
