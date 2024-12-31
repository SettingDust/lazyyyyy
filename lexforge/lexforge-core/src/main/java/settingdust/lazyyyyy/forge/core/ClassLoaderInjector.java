package settingdust.lazyyyyy.forge.core;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import settingdust.lazyyyyy.forge.core.faster_mixin.ModuleClassLoaderReflection;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
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
        var bootstrapJarPath = SELF_PATH.resolve("lazyyyyy-lexforge-mc-bootstrap.jar");
        try (var bootstrapJar = new JarFile(bootstrapJarPath.toFile())) {
            Instrumentation instrumentation;
            try {
                instrumentation = ByteBuddyAgent.getInstrumentation();
            } catch (Throwable t) {
                LazyyyyyHacksInjector.LOGGER.error(
                    "No bytebuddy agent, the bootstrap jar can't be injected",
                    t
                );
                return;
            }
        }
    }

    /**
     * https://github.com/Sinytra/MixinTransmogrifier/blob/agentful/src/main/java/io/github/steelwoolmc/mixintransmog/InstrumentationHack.java
     */
    public static void injectMcBootstrap() throws Throwable {
        LazyyyyyHacksInjector.LOGGER.info("Injecting mc bootstrap");
        var mcBootstrapJar = SELF_PATH.resolve("lazyyyyy-lexforge-mc-bootstrap.jar");
        var mixinJar = SecureJar.from(mcBootstrapJar);

        var jarModuleFinder = JarModuleFinder.of(mixinJar);

        var bootstrapClassLoader = (ModuleClassLoader) Launcher.class.getClassLoader();
        var parentConfiguration = ModuleClassLoaderReflection.getConfiguration(bootstrapClassLoader);
        var configuration = parentConfiguration.resolve(
            jarModuleFinder,
            JarModuleFinder.of(),
            Set.of("lazyyyyy.lexforge.mc.bootstrap")
        );
        var resolvedModule = configuration.findModule("lazyyyyy.lexforge.mc.bootstrap").orElseThrow();

        //                // Add readability edge to the unnamed module, where classes from added packages are defined
        //                var handle = TRUSTED_LOOKUP.findVirtual(
        //                    Module.class,
        //                    "implAddReads",
        //                    MethodType.methodType(void.class, Module.class)
        //                );
        //
        //                handle.invokeExact(resolvedModule.reference().open(), resolvedModule.getClassLoader().getUnnamedModule());


        ModuleClassLoaderReflection.setConfiguration(bootstrapClassLoader, configuration);
        // Make modlauncher aware of added packages
        // FIXME Causing ConcurrentModificationException since the {@link net.minecraftforge.fml.earlydisplay.DisplayWindow.start} is loading lwjgl in async
        var packageLookupField = ModuleClassLoader.class.getDeclaredField("packageLookup");
        Map<String, ResolvedModule> packageLookup = UnsafeHacks.getField(packageLookupField, bootstrapClassLoader);

        for (String pkg : mixinJar.getPackages()) {
            packageLookup.put(pkg, resolvedModule);
        }

        var resolvedRootsField = ModuleClassLoader.class.getDeclaredField("resolvedRoots");
        Map<String, ModuleReference> resolvedRoots = UnsafeHacks.getField(resolvedRootsField, bootstrapClassLoader);
        resolvedRoots.put(resolvedModule.reference().descriptor().name(), resolvedModule.reference());
    }
}