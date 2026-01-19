package settingdust.lazyyyyy.faster_module_resolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.lazyyyyy.config.LazyyyyyEarlyConfig;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap;

public class FasterModuleResolverEntrypoint implements PreloadingEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");

    public FasterModuleResolverEntrypoint() {
        if (!LazyyyyyEarlyConfig.instance().isFeatureEnabled(LazyyyyyEarlyConfig.FASTER_MODULE_RESOLVER)) {
            return;
        }

        LOGGER.info("Applying " + LazyyyyyEarlyConfig.FASTER_MODULE_RESOLVER);

        ClassTransformBootstrap.INSTANCE.addConfig("lazyyyyy.faster_module_resolver.classtransform.json");
    }
}
