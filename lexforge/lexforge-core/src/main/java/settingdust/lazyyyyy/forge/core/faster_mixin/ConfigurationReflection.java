package settingdust.lazyyyyy.forge.core.faster_mixin;

import net.lenni0451.reflect.Fields;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class ConfigurationReflection {
    public static final Class<Configuration> configurationClass = Configuration.class;

    private static final Field graphField;
    private static final Field modulesField;
    private static final Field nameToModuleField;

    static {
        try {
            graphField = configurationClass.getDeclaredField("graph");
            modulesField = configurationClass.getDeclaredField("modules");
            nameToModuleField = configurationClass.getDeclaredField("nameToModule");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<ResolvedModule, Set<ResolvedModule>> getGraph(Configuration configuration) {
        return Fields.getObject(configuration, graphField);
    }

    public static void setGraph(Configuration configuration, Map<ResolvedModule, Set<ResolvedModule>> graph) {
        Fields.setObject(configuration, graphField, graph);
    }

    public static Set<ResolvedModule> getModules(Configuration configuration) {
        return Fields.getObject(configuration, modulesField);
    }

    public static void setModules(Configuration configuration, Set<ResolvedModule> modules) {
        Fields.setObject(configuration, modulesField, modules);
    }

    public static Map<String, ResolvedModule> getNameToModule(Configuration configuration) {
        return Fields.getObject(configuration, nameToModuleField);
    }

    public static void setNameToModule(Configuration configuration, Map<String, ResolvedModule> nameToModule) {
        Fields.setObject(configuration, nameToModuleField, nameToModule);
    }
}
