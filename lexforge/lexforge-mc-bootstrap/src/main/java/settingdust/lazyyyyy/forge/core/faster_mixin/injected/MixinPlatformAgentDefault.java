package settingdust.lazyyyyy.forge.core.faster_mixin.injected;

import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MixinPlatformAgentDefault extends MixinPlatformAgentAbstract {
    private static final Method setCompatibilityLevelMethod;
    private static final Method addConfigMethod;
    private static final Method addTokenProviderMethod;
    private static final Method addConnectorMethod;

    public static final Map<String, IContainerHandle> CONFIG_TO_CONTAINER = new HashMap<>();

    static {
        try {
            setCompatibilityLevelMethod = MixinPlatformManager.class.getDeclaredMethod(
                "setCompatibilityLevel",
                String.class
            );
            setCompatibilityLevelMethod.setAccessible(true);
            addConfigMethod = MixinPlatformManager.class.getDeclaredMethod(
                "addConfig",
                String.class
            );
            addConfigMethod.setAccessible(true);
            addTokenProviderMethod = MixinPlatformManager.class.getDeclaredMethod("addTokenProvider", String.class);
            addTokenProviderMethod.setAccessible(true);
            addConnectorMethod = MixinPlatformManager.class.getDeclaredMethod("addConnector", String.class);
            addConnectorMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void prepare() {
        @SuppressWarnings("deprecation")
        String compatibilityLevel = this.handle.getAttribute(Constants.ManifestAttributes.COMPATIBILITY);
        if (compatibilityLevel != null) {
            try {
                setCompatibilityLevelMethod.invoke(manager, compatibilityLevel);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        String mixinConfigs = this.handle.getAttribute(Constants.ManifestAttributes.MIXINCONFIGS);
        if (mixinConfigs != null) {
            for (String config : mixinConfigs.split(",")) {
                try {
                    var configName = config.trim();
                    CONFIG_TO_CONTAINER.putIfAbsent(configName, handle);
                    addConfigMethod.invoke(manager, configName);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String tokenProviders = this.handle.getAttribute(Constants.ManifestAttributes.TOKENPROVIDERS);
        if (tokenProviders != null) {
            for (String provider : tokenProviders.split(",")) {
                try {
                    addTokenProviderMethod.invoke(manager, provider.trim());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String connectorClass = this.handle.getAttribute(Constants.ManifestAttributes.MIXINCONNECTOR);
        if (connectorClass != null) {
            try {
                addConnectorMethod.invoke(manager, connectorClass.trim());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
