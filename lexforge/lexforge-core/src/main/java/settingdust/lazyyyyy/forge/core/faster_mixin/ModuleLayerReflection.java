package settingdust.lazyyyyy.forge.core.faster_mixin;

import net.lenni0451.reflect.Fields;

import java.lang.reflect.Field;
import java.util.Map;

public class ModuleLayerReflection {
    public static final Class<ModuleLayer> moduleLayerClass = ModuleLayer.class;

    private static final Field nameToModuleField;

    static {
        try {
            nameToModuleField = moduleLayerClass.getDeclaredField("nameToModule");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Module> getNameToModule(ModuleLayer moduleLayer) {
        return Fields.getObject(moduleLayer, nameToModuleField);
    }
}
