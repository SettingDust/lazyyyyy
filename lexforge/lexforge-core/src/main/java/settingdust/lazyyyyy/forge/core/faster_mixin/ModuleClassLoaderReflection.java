package settingdust.lazyyyyy.forge.core.faster_mixin;

import cpw.mods.cl.ModuleClassLoader;
import net.minecraftforge.fml.unsafe.UnsafeHacks;

import java.lang.module.Configuration;
import java.lang.reflect.Field;

public class ModuleClassLoaderReflection {
    public static final Class<?> moduleClassLoaderClass = ModuleClassLoader.class;
    public static final Field configurationField;

    static {
        try {
            configurationField = moduleClassLoaderClass.getDeclaredField("configuration");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration getConfiguration(ModuleClassLoader moduleClassLoader) {
        return UnsafeHacks.getField(configurationField, moduleClassLoader);
    }

    public static void setConfiguration(ModuleClassLoader moduleClassLoader, Configuration configuration) {
        UnsafeHacks.setField(configurationField, moduleClassLoader, configuration);
    }
}
