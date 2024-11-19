package settingdust.lazyyyyy.forge.core.faster_mixin;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

/**
 * https://github.com/Sinytra/MixinTransmogrifier/blob/agentful/src/main/java/io/github/steelwoolmc/mixintransmog/InstrumentationHack.java
 */
public class InstrumentationHack {
    private static final MethodHandles.Lookup TRUSTED_LOOKUP = uncheck(() -> {
        var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        var unsafe = (Unsafe) theUnsafe.get(null);
        var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        return (MethodHandles.Lookup) unsafe.getObject(
            unsafe.staticFieldBase(hackfield),
            unsafe.staticFieldOffset(hackfield)
        );
    });

    private static final Path SELF_PATH = uncheck(() -> {
        var jarLocation = InstrumentationHack.class.getProtectionDomain().getCodeSource().getLocation();
        return Path.of(jarLocation.toURI());
    });

    public static void inject() throws Throwable {
        var mixinJarPath = SELF_PATH.resolve("lazyyyyy-lexforge-mixin.jar");
        var mixinJar = SecureJar.from(mixinJarPath);

        var jarModuleFinder = JarModuleFinder.of(mixinJar);

        var bootstrapClassLoader = (ModuleClassLoader) Launcher.class.getClassLoader();
        var parentConfiguration = ModuleClassLoaderReflection.getConfiguration(bootstrapClassLoader);
        var configuration = parentConfiguration.resolve(
            jarModuleFinder,
            JarModuleFinder.of(),
            Set.of("lazyyyyy.lexforge.mixin")
        );
        var resolvedModule = configuration.findModule("lazyyyyy.lexforge.mixin").orElseThrow();

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
