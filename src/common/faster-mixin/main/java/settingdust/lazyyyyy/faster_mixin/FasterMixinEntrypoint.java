package settingdust.lazyyyyy.faster_mixin;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FasterMixinEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");

    public static void init(ClassLoader loader) {
        var featureConfigClass = RStream.of(Classes.byName("settingdust.lazyyyyy.api.config.FeatureConfig", loader));
        var earlyConfigClass = RStream.of(Classes.byName("settingdust.lazyyyyy.config.LazyyyyyEarlyConfig", loader));
        if (!featureConfigClass.methods()
                .by("isFeatureEnabled")
                .<Boolean>invokeInstance(earlyConfigClass.methods().by("instance").invoke(), "faster_mixin")) {
            return;
        }

        LOGGER.info("Applying faster_mixin");

        var clazz = RStream.of(Classes.byName(
                "settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap",
                loader));
        clazz.methods()
                .by("addConfig", String.class)
                .invokeInstance(clazz.fields().by("INSTANCE").get(), "lazyyyyy.faster_mixin.classtransform.json");
    }
}
