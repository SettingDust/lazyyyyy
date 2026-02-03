package settingdust.lazyyyyy.faster_mixin;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import settingdust.lazyyyyy.faster_mixin.util.LazyyyyyEarlyConfigInvoker;

import java.util.function.Consumer;

public class FasterMixinEntrypoint {
    public static void init(ClassLoader loader, Consumer<String> logger) {
        LazyyyyyEarlyConfigInvoker.init(loader);
        if (!LazyyyyyEarlyConfigInvoker.isFeatureEnabled("faster_mixin")) {
            return;
        }

        logger.accept("Applying faster_mixin");

        var clazz = RStream.of(Classes.byName(
                "settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap",
                loader));
        clazz.methods()
                .by("addConfig", String.class)
                .invokeInstance(clazz.fields().by("INSTANCE").get(), "lazyyyyy.faster_mixin.classtransform.json");
    }
}
