package settingdust.lazyyyyy.forge.core;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import net.lenni0451.reflect.Agents;
import settingdust.lazyyyyy.forge.core.faster_mixin.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

public class ClassLoaderInjector {
    private static final Path SELF_PATH = uncheck(() -> {
        var jarLocation = ClassLoaderInjector.class.getProtectionDomain().getCodeSource().getLocation();
        return Path.of(jarLocation.toURI());
    });

    public static void injectBootstrap() throws IOException {
        LazyyyyyHacksInjector.LOGGER.info("Injecting bootstrap");
        var bootstrapJarPath = SELF_PATH.resolve("lazyyyyy-lexforge-bootstrap.jar");
        var exportedJarPath = Paths.get(".lazyyyyy", "lazyyyyy-lexforge-bootstrap.jar");
        if (Files.notExists(exportedJarPath)) {
            try {
                Files.createDirectory(exportedJarPath.getParent());
            } catch (IOException ignored) {}
            Files.copy(bootstrapJarPath, exportedJarPath);
        }
        try (var bootstrapJar = new JarFile(exportedJarPath.toFile())) {
            Instrumentation instrumentation;
            try {
                instrumentation = Agents.getInstrumentation();
            } catch (Throwable t) {
                LazyyyyyHacksInjector.LOGGER.debug("No dummy agent, the bootstrap jar can't be injected", t);
                return;
            }
            instrumentation.appendToBootstrapClassLoaderSearch(bootstrapJar);
        }
    }

    /**
     * https://github.com/Sinytra/MixinTransmogrifier/blob/agentful/src/main/java/io/github/steelwoolmc/mixintransmog/InstrumentationHack.java
     */
    public static void injectMcBootstrap() {
        LazyyyyyHacksInjector.LOGGER.info("Injecting mc bootstrap");
        var mcBootstrapJar = SELF_PATH.resolve("lazyyyyy-lexforge-mc-bootstrap.jar");
        var mixinJar = SecureJar.from(mcBootstrapJar);

        var jarModuleFinder = JarModuleFinder.of(mixinJar);

        var mcBootClassLoader = (ModuleClassLoader) Launcher.class.getClassLoader();
        var bootConfiguration = ModuleClassLoaderReflection.getConfiguration(mcBootClassLoader);
        var configuration = bootConfiguration.resolve(
            jarModuleFinder,
            JarModuleFinder.of(),
            Set.of("lazyyyyy.lexforge.mc.bootstrap")
        );
        var resolvedModule = configuration.findModule("lazyyyyy.lexforge.mc.bootstrap").orElseThrow();

        mergeConfigurations(bootConfiguration, configuration);

        var moduleLayer = Launcher.INSTANCE.findLayerManager()
                                           .flatMap(it -> it.getLayer(IModuleLayerManager.Layer.BOOT))
                                           .orElseThrow();
        var module = ModuleReflection.construct(
            moduleLayer,
            mcBootClassLoader,
            resolvedModule.reference().descriptor(),
            resolvedModule.reference().location().orElse(null)
        );

        ModuleLayerReflection.getNameToModule(moduleLayer).put(resolvedModule.name(), module);

        ClassLoaderInjector.class.getModule().addReads(module);

        for (final var readModule : ConfigurationReflection.getGraph(configuration).get(resolvedModule)) {
            ModuleReflection.implAddRead(module, moduleLayer.findModule(readModule.name()).orElseThrow());
        }

        ResolvedModuleReflection.setConfiguration(resolvedModule, bootConfiguration);
        // Make modlauncher aware of added packages
        Map<String, ResolvedModule> packageLookup =
            new HashMap<>(ModuleClassLoaderReflection.getPackageLookup(mcBootClassLoader));

        for (String pkg : mixinJar.getPackages()) {
            packageLookup.put(pkg, resolvedModule);
        }

        ModuleClassLoaderReflection.setPackageLookup(mcBootClassLoader, new HashMap<>(packageLookup));

        Map<String, ModuleReference> resolvedRoots =
            new HashMap<>(ModuleClassLoaderReflection.getResolvedRoots(mcBootClassLoader));
        resolvedRoots.put(resolvedModule.reference().descriptor().name(), resolvedModule.reference());
        ModuleClassLoaderReflection.setResolvedRoots(mcBootClassLoader, new HashMap<>(resolvedRoots));
    }

    private static void mergeConfigurations(final Configuration to, final Configuration from) {
        ConfigurationReflection.getGraph(to).putAll(ConfigurationReflection.getGraph(from));

        Set<ResolvedModule> modules = new HashSet<>(ConfigurationReflection.getModules(to));
        modules.addAll(ConfigurationReflection.getModules(from));
        ConfigurationReflection.setModules(to, modules);

        Map<String, ResolvedModule> nameToModule = new HashMap<>(ConfigurationReflection.getNameToModule(to));
        nameToModule.putAll(ConfigurationReflection.getNameToModule(from));
        ConfigurationReflection.setNameToModule(to, nameToModule);
    }
}
