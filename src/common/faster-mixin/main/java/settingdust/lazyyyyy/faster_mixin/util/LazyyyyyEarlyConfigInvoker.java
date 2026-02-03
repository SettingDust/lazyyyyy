package settingdust.lazyyyyy.faster_mixin.util;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.method.MethodWrapper;

public class LazyyyyyEarlyConfigInvoker {
    private static RStream featureConfigClass;
    private static RStream earlyConfigClass;
    private static MethodWrapper isFeatureEnabled;
    private static Object instance;

    public static void init(ClassLoader loader) {
        featureConfigClass = RStream.of(Classes.byName("settingdust.lazyyyyy.api.config.FeatureConfig", loader));
        earlyConfigClass = RStream.of(Classes.byName("settingdust.lazyyyyy.config.LazyyyyyEarlyConfig", loader));
        instance = earlyConfigClass.methods().by("instance").invoke();
        isFeatureEnabled = featureConfigClass.methods().by("isFeatureEnabled", String.class);
    }

    public static boolean isFeatureEnabled(String featureName) {
        return isFeatureEnabled.invokeInstance(instance, featureName);
    }
}
