package settingdust.lazyyyyy.forge.core.faster_mixin;

import cpw.mods.cl.ModuleClassLoader;
import net.lenni0451.reflect.Fields;

import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.util.Map;

public class ModuleClassLoaderReflection {
    public static final Class<ModuleClassLoader> moduleClassLoaderClass = ModuleClassLoader.class;
    public static final Field configurationField;
    public static final Field packageLookupField;
    public static final Field resolvedRootsField;

    static {
        try {
            configurationField = moduleClassLoaderClass.getDeclaredField("configuration");
            packageLookupField = moduleClassLoaderClass.getDeclaredField("packageLookup");
            resolvedRootsField = moduleClassLoaderClass.getDeclaredField("resolvedRoots");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration getConfiguration(ModuleClassLoader moduleClassLoader) {
        return Fields.getObject(moduleClassLoader, configurationField);
    }

    public static void setConfiguration(ModuleClassLoader moduleClassLoader, Configuration configuration) {
        Fields.setObject(moduleClassLoader, configurationField, configuration);
    }

    public static Map<String, ResolvedModule> getPackageLookup(ModuleClassLoader moduleClassLoader) {
        return Fields.getObject(moduleClassLoader, packageLookupField);
    }

    public static void setPackageLookup(
        ModuleClassLoader moduleClassLoader,
        Map<String, ResolvedModule> value
    ) {
        Fields.setObject(moduleClassLoader, packageLookupField, value);
    }

    public static Map<String, ModuleReference> getResolvedRoots(ModuleClassLoader moduleClassLoader) {
        return Fields.getObject(moduleClassLoader, resolvedRootsField);
    }

    public static void setResolvedRoots(
        ModuleClassLoader moduleClassLoader,
        Map<String, ModuleReference> resolvedRoots
    ) {
        Fields.setObject(moduleClassLoader, resolvedRootsField, resolvedRoots);
    }
}
