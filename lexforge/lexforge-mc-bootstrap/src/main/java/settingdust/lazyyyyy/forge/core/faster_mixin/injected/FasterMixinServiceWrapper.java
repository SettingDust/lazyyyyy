package settingdust.lazyyyyy.forge.core.faster_mixin.injected;

import cpw.mods.jarhandling.SecureJar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.IClassProcessor;
import org.spongepowered.asm.launch.platform.MixinContainer;
import org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;
import org.spongepowered.asm.util.IConsumer;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public class FasterMixinServiceWrapper extends MixinServiceModLauncher implements IMixinService {
    public static MixinServiceModLauncher wrapped;
    private FasterClassProviderWrapper classProvider;

    public static final Logger LOGGER = LogManager.getLogger();

    public static final Map<String, Set<IMixinConfig>> PLUGIN_TO_CONFIGS = new HashMap<>();
    public static final Map<IMixinConfig, String> CONFIG_TO_REFMAP = new HashMap<>();
    public static final Map<String, Set<IMixinConfig>> REFMAP_TO_CONFIGS = new HashMap<>();

    private static final Class<?> secureJarResourceClass;
    private static final Field secureJarField;

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

    public FasterMixinServiceWrapper() throws NoSuchFieldException, IllegalAccessException {
        var agentClassesField = MixinContainer.class.getDeclaredField("agentClasses");
        agentClassesField.setAccessible(true);
        var agentClasses = (List<String>) agentClassesField.get(null);
        if (!agentClasses.contains("settingdust.lazyyyyy.forge.core.faster_mixin.injected.MixinPlatformAgentDefault")) {
            agentClasses.remove("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
            agentClasses.add("settingdust.lazyyyyy.forge.core.faster_mixin.injected.MixinPlatformAgentDefault");
        }
    }

    @Override
    public String getName() {return wrapped.getName();}

    @Override
    public boolean isValid() {return wrapped.isValid();}

    @Override
    public void prepare() {wrapped.prepare();}

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return wrapped.getInitialPhase();
    }

    @Override
    public void offer(final IMixinInternal internal) {wrapped.offer(internal);}

    @Override
    public void init() {
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
    public ContainerHandleModLauncher getPrimaryContainer() {return wrapped.getPrimaryContainer();}

    @Override
    public Collection<IContainerHandle> getMixinContainers() {return wrapped.getMixinContainers();}

    @Override
    public InputStream getResourceAsStream(final String name) {
        var handle = MixinPlatformAgentDefault.CONFIG_TO_CONTAINER.get(name);
        InputStream result = null;
        if (handle != null) {
            try {
                result = Files.newInputStream(((SecureJar) secureJarField.get(handle)).getPath(name));
                LOGGER.debug("Read config {} from {}", name, ((ContainerHandleURI) handle).getURI());
            } catch (IllegalAccessException | IOException e) {
                LOGGER.warn(
                    "Failed to read config {} from {}. Find from all resources will spend extra time. Check the debug log for detail of reason. If the file is missing, let the author of the mod moving the mixin config into the jar declare it",
                    name,
                    ((ContainerHandleURI) handle).getURI()
                );
                LOGGER.debug("Failed to read config {} from {}", name, ((ContainerHandleURI) handle).getURI(), e);
            }
        }

        var configs = REFMAP_TO_CONFIGS.get(name);

        if (configs != null && !configs.isEmpty()) {
            for (final var mixinConfig : configs) {
                var source = MixinPlatformAgentDefault.CONFIG_TO_CONTAINER.get(mixinConfig.getName());
                if (source == null) continue;
                try {
                    result = Files.newInputStream(((SecureJar) secureJarField.get(source)).getPath(name));
                    LOGGER.debug(
                        "Read refmap {} from {} for config {}",
                        name,
                        ((ContainerHandleURI) source).getURI(),
                        mixinConfig
                    );
                    break;
                } catch (IOException | IllegalAccessException e) {
                    LOGGER.warn(
                        "Failed to read refmap {} from {} for config {}. Find from all resources will spend extra time. Check the debug log for detail of reason. If the file is missing, let the author of the mod moving the refmap into the jar declare it",
                        name,
                        ((ContainerHandleURI) source).getURI(),
                        mixinConfig
                    );
                    LOGGER.debug(
                        "Failed to read refmap {} from {} for config {}",
                        name,
                        ((ContainerHandleURI) source).getURI(),
                        mixinConfig,
                        e
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
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {return wrapped.getMinCompatibilityLevel();}

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {return wrapped.getMaxCompatibilityLevel();}

    @Override
    public ILogger getLogger(final String name) {return wrapped.getLogger(name);}

    @Override
    public void onInit(final IClassBytecodeProvider bytecodeProvider) {wrapped.onInit(bytecodeProvider);}

    @Override
    public void onStartup() {
        for (final var config : Mixins.getConfigs()) {
            var plugin = MixinConfigReflection.getPlugin(config.getConfig());
            PLUGIN_TO_CONFIGS.computeIfAbsent(plugin, (key) -> new HashSet<>()).add(config.getConfig());

            var refmap = MixinConfigReflection.getRefmap(config.getConfig());
            REFMAP_TO_CONFIGS.computeIfAbsent(refmap, key -> new HashSet<>()).add(config.getConfig());

            FasterMixinServiceWrapper.LOGGER.debug(
                "Caching info for config {}. Plugin: {}. Refmap: {}",
                config.getConfig(),
                plugin,
                refmap
            );
        }
        wrapped.onStartup();
    }

    @Override
    public void wire(
        final MixinEnvironment.Phase phase,
        final IConsumer<MixinEnvironment.Phase> phaseConsumer
    ) {wrapped.wire(phase, phaseConsumer);}

    @Override
    public Collection<IClassProcessor> getProcessors() {return wrapped.getProcessors();}

    @Deprecated
    @Override
    public void unwire() {wrapped.unwire();}
}
