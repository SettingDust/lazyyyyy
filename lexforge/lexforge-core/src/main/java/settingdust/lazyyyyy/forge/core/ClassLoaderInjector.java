package settingdust.lazyyyyy.forge.core;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.minecraftforge.fml.loading.FMLPaths;
import settingdust.lazyyyyy.forge.core.faster_mixin.ModuleClassLoaderReflection;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
        var exportedJarPath = FMLPaths.GAMEDIR.get().resolve(".lazyyyyy").resolve("lazyyyyy-lexforge-bootstrap.jar");
        if (Files.notExists(exportedJarPath)) {
            try {Files.createDirectory(exportedJarPath.getParent());} catch (IOException ignored) {}
            Files.copy(bootstrapJarPath, exportedJarPath);
        }
        try (var bootstrapJar = new JarFile(exportedJarPath.toFile())) {
            Instrumentation instrumentation;
            try {
                instrumentation = ByteBuddyAgent.getInstrumentation();
            } catch (Throwable t) {
                LazyyyyyHacksInjector.LOGGER.debug("No bytebuddy agent, the bootstrap jar can't be injected", t);
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
        var mcBootstrapJarPath = SELF_PATH.resolve("lazyyyyy-lexforge-mc-bootstrap.jar");
        var mcBootstrapJar = SecureJar.from(mcBootstrapJarPath);

        var jarModuleFinder = JarModuleFinder.of(mcBootstrapJar);

        var bootstrapClassLoader = (ModuleClassLoader) Launcher.class.getClassLoader();
        var parentConfiguration = ModuleClassLoaderReflection.getConfiguration(bootstrapClassLoader);
        var configuration = parentConfiguration.resolve(
            jarModuleFinder,
            JarModuleFinder.of(),
            Set.of("lazyyyyy.lexforge.mc.bootstrap")
        );
        var resolvedModule = configuration.findModule("lazyyyyy.lexforge.mc.bootstrap").orElseThrow();

        ModuleClassLoaderReflection.setConfiguration(bootstrapClassLoader, configuration);
        // Make modlauncher aware of added packages
        Map<String, ResolvedModule> packageLookup =
            new HashMap<>(ModuleClassLoaderReflection.getPackageLookup(bootstrapClassLoader));

        for (String pkg : mcBootstrapJar.getPackages()) {
            packageLookup.put(pkg, resolvedModule);
        }

        ModuleClassLoaderReflection.setPackageLookup(bootstrapClassLoader, packageLookup);

        Map<String, ModuleReference> resolvedRoots =
            new HashMap<>(ModuleClassLoaderReflection.getResolvedRoots(bootstrapClassLoader));
        resolvedRoots.put(resolvedModule.reference().descriptor().name(), resolvedModule.reference());
        ModuleClassLoaderReflection.setResolvedRoots(bootstrapClassLoader, resolvedRoots);
    }
}
