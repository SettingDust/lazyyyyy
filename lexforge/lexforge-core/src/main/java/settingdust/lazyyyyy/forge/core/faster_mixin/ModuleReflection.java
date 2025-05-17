package settingdust.lazyyyyy.forge.core.faster_mixin;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;

import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;

public class ModuleReflection {
    public static final Class<Module> moduleClass = Module.class;

    private static final Constructor<Module> constructor;

    private static final Method implAddReadMethod;

    static {
        constructor = Constructors.getDeclaredConstructor(
            moduleClass,
            ModuleLayer.class,
            ClassLoader.class,
            ModuleDescriptor.class,
            URI.class
        );
        try {
            implAddReadMethod = moduleClass.getDeclaredMethod("implAddReads", Module.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Module construct(
        ModuleLayer moduleLayer,
        ClassLoader classLoader,
        ModuleDescriptor moduleDescriptor,
        URI uri
    ) {
        return Constructors.invoke(constructor, moduleLayer, classLoader, moduleDescriptor, uri);
    }

    public static void implAddRead(Module module, Module other) {
        Methods.invoke(module, implAddReadMethod, other);
    }
}
