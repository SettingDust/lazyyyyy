package settingdust.lazyyyyy.forge.core.faster_mixin;

import cpw.mods.cl.ModuleClassLoader;
import net.minecraftforge.fml.unsafe.UnsafeHacks;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.util.Map;

public class ModuleClassLoaderReflection {
    public static final Class<?> moduleClassLoaderClass = ModuleClassLoader.class;
    public static final Field configurationField;
    public static final Field packageLookup;

    static {
        try {
            configurationField = moduleClassLoaderClass.getDeclaredField("configuration");
            packageLookup = moduleClassLoaderClass.getDeclaredField("packageLookup");
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

    public static Map<String, ResolvedModule> getPackageLookup(ModuleClassLoader moduleClassLoader) {
        return UnsafeHacks.getField(packageLookup, moduleClassLoader);
    }

    public static void setPackageLookup(ModuleClassLoader moduleClassLoader, Object packageLookup) {
        UnsafeHacks.setField(ModuleClassLoaderReflection.packageLookup, moduleClassLoader, packageLookup);
    }
}
