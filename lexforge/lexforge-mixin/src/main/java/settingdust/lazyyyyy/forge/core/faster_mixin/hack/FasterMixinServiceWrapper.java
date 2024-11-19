package settingdust.lazyyyyy.forge.core.faster_mixin.hack;

import cpw.mods.jarhandling.SecureJar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public class FasterMixinServiceWrapper implements IMixinService {
    public static IMixinService wrapped;
    private FasterClassProviderWrapper classProvider;
    
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static final Map<String, Set<IMixinConfig>> pluginToConfigs = new HashMap<>();
    public static final Map<IMixinConfig, String> configToRefmap = new HashMap<>();
    public static final Map<String, Set<IMixinConfig>> refmapToConfigs = new HashMap<>();
    
    private static final Class<?> secureJarResourceClass;
    private static final Field secureJarField;

    static {
        try {
            secureJarResourceClass = Class.forName(
                "org/spongepowered/asm/launch/platform/container/ContainerHandleModLauncherEx$SecureJarResource");
            secureJarField = secureJarResourceClass.getDeclaredField("jar");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {return wrapped.getName();}

    @Override
    public boolean isValid() {return wrapped.isValid();}

    @Override
    public void prepare() {wrapped.prepare();}

    @Override
    public MixinEnvironment.Phase getInitialPhase() {return wrapped.getInitialPhase();}

    @Override
    public void offer(final IMixinInternal internal) {wrapped.offer(internal);}

    @Override
    public void init() {
        for (final var config : Mixins.getConfigs()) {
            var plugin = MixinConfigReflection.getPlugin(config.getConfig());
            var configsForPlugin = pluginToConfigs.getOrDefault(
                plugin,
                new HashSet<>()
            );
            configsForPlugin.add(config.getConfig());
            pluginToConfigs.put(plugin, configsForPlugin);

            var refmap = MixinConfigReflection.getRefmap(config.getConfig());
            var configsForRefmap = refmapToConfigs.getOrDefault(refmap, new HashSet<>());
            configsForRefmap.add(config.getConfig());
            refmapToConfigs.put(refmap, configsForRefmap);
        }
        wrapped.init();
    }

    @Override
    public void beginPhase() {wrapped.beginPhase();}

    @Override
    public void checkEnv(final Object bootSource) {wrapped.checkEnv(bootSource);}

    @Override
    public ReEntranceLock getReEntranceLock() {return wrapped.getReEntranceLock();}

    @Override
    public IClassProvider getClassProvider() {
        if (this.classProvider == null) {
            classProvider = new FasterClassProviderWrapper(wrapped.getClassProvider());
        }
        return classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {return wrapped.getBytecodeProvider();}

    @Override
    public ITransformerProvider getTransformerProvider() {return wrapped.getTransformerProvider();}

    @Override
    public IClassTracker getClassTracker() {return wrapped.getClassTracker();}

    @Override
    public IMixinAuditTrail getAuditTrail() {return wrapped.getAuditTrail();}

    @Override
    public Collection<String> getPlatformAgents() {return wrapped.getPlatformAgents();}

    @Override
    public IContainerHandle getPrimaryContainer() {return wrapped.getPrimaryContainer();}

    @Override
    public Collection<IContainerHandle> getMixinContainers() {return wrapped.getMixinContainers();}

    @Override
    public InputStream getResourceAsStream(final String name) {
        var source = MixinConfigReflection.getAllConfigs().get(name).getConfig().getSource();
        InputStream result = null;
        if (source != null) {
            try {
                result = Files.newInputStream(((SecureJar) secureJarField.get(source)).getPath(name));
            } catch (IllegalAccessException | IOException ignored) {
                LOGGER.debug("Failed to read config {} from {}", name, source);
                result = wrapped.getResourceAsStream(name);
            }
        }

        var configs = refmapToConfigs.get(name);

        if (configs != null && !configs.isEmpty()) {
            for (final var config : configs) {
                source = config.getSource();
                if (source == null) continue;
                try {
                    result = Files.newInputStream(((SecureJar) secureJarField.get(source)).getPath(name));
                    break;
                } catch (IOException | IllegalAccessException ignored) {
                    LOGGER.debug(
                        "Failed to read refmap {} from {} for config {}",
                        name,
                        source,
                        config
                    );
                }
            }
        }

        if (result == null) {
            result = wrapped.getResourceAsStream(name);
        }
        return result;
    }

    @Override
    public String getSideName() {return wrapped.getSideName();}

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {return wrapped.getMinCompatibilityLevel();}

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {return wrapped.getMaxCompatibilityLevel();}

    @Override
    public ILogger getLogger(final String name) {return wrapped.getLogger(name);}
}
