package settingdust.lazyyyyy.forge.core.faster_mixin;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Fields;

import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ResolvedModuleReflection {
    public static final Class<ResolvedModule> resolvedModuleClass = ResolvedModule.class;

    private static final Constructor<ResolvedModule> constructor;

    private static final Field cfField;

    static {
        try {
            constructor = Constructors.getDeclaredConstructor(
                resolvedModuleClass,
                Configuration.class,
                ModuleReference.class
            );

            cfField = resolvedModuleClass.getDeclaredField("cf");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResolvedModule construct(Configuration configuration, ModuleReference moduleReference) {
        return Constructors.invoke(constructor, configuration, moduleReference);
    }

    public static Configuration getConfiguration(ResolvedModule resolvedModule) {
        return Fields.getObject(resolvedModule, cfField);
    }

    public static void setConfiguration(ResolvedModule resolvedModule, Configuration configuration) {
        Fields.setObject(resolvedModule, cfField, configuration);
    }
}
