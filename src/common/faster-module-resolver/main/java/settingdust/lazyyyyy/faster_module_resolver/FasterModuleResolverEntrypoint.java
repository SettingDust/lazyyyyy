package settingdust.lazyyyyy.faster_module_resolver;

import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

public class FasterModuleResolverEntrypoint implements PreloadingEntrypoint {
    public FasterModuleResolverEntrypoint() {
        if (Runtime.version().feature() > 21) return;
        
        ClassTransformBootstrap.INSTANCE.addConfig("lazyyyyy.faster_module_resolver.classtransform.json");
    }
}
