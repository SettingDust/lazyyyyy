package settingdust.lazyyyyy.forge.core.faster_mixin.injected;

import java.lang.reflect.Field;

public class SecureJarResourceReflection {
    public static final Class<?> secureJarResourceClass;
    public static final Field secureJarField;

    static {
        try {
            secureJarResourceClass = Class.forName(
                "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncherEx$SecureJarResource");
            secureJarField = secureJarResourceClass.getDeclaredField("jar");
            secureJarField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
