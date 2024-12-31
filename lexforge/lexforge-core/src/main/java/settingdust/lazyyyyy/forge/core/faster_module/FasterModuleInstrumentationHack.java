package settingdust.lazyyyyy.forge.core.faster_module;

import net.bytebuddy.agent.ByteBuddyAgent;
import settingdust.lazyyyyy.forge.core.LazyyyyyHacksInjector;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

public class FasterModuleInstrumentationHack {
    private static final Unsafe UNSAFE = uncheck(() -> {
        var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    });
    private static final MethodHandles.Lookup TRUSTED_LOOKUP = uncheck(() -> {
        var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        return (MethodHandles.Lookup) UNSAFE.getObject(
            UNSAFE.staticFieldBase(hackfield),
            UNSAFE.staticFieldOffset(hackfield)
        );
    });

    /**
     * https://github.com/openjdk/jdk/pull/16818
     */
    public static void replaceModuleResolver() throws
                                               ClassNotFoundException,
                                               UnmodifiableClassException,
                                               IOException,
                                               URISyntaxException, IllegalAccessException, InvocationTargetException {
        Instrumentation instrumentation;
        try {
            instrumentation = ByteBuddyAgent.getInstrumentation();
        } catch (Throwable t) {
            LazyyyyyHacksInjector.LOGGER.error(
                "No bytebuddy agent, the major optimization for module resolver won't work",
                t
            );
            return;
        }
        var bootClassLoader = new ClassLoader(null) {};
        Method defineClassMethod = null;
        Class<?> classLoaderClass = bootClassLoader.getClass();
        do {
            try {
                defineClassMethod = classLoaderClass.getDeclaredMethod(
                    "defineClass",
                    String.class,
                    byte[].class,
                    int.class,
                    int.class,
                    ProtectionDomain.class
                );
            } catch (NoSuchMethodException ignored) {}
            classLoaderClass = classLoaderClass.getSuperclass();
        } while (defineClassMethod == null && classLoaderClass != Object.class);
        if (defineClassMethod == null) {
            LazyyyyyHacksInjector.LOGGER.warn("Failed to find defineClass");
            return;
        }
        // Needed for `setAccessible`
        instrumentation.redefineModule(
            classLoaderClass.getModule(),
            Set.of(),
            Map.of(),
            Map.of("java.lang", Set.of(FasterModuleInstrumentationHack.class.getModule())),
            Set.of(),
            Map.of()
        );
        defineClassMethod.setAccessible(true);
        var bytes = FasterModuleInstrumentationHack.class
            .getClassLoader()
            .getResourceAsStream("settingdust/lazyyyyy/forge/core/faster_module/BootstrapHooks.class")
            .readAllBytes();
        defineClassMethod.invoke(
            bootClassLoader,
            "settingdust.lazyyyyy.forge.core.faster_module.BootstrapHooks",
            bytes,
            0,
            bytes.length,
            FasterModuleInstrumentationHack.class.getProtectionDomain()
        );
        //        TRUSTED_LOOKUP.defineClass(
        //            FasterModuleInstrumentationHack.class
        //                .getClassLoader()
        //                .getResourceAsStream("settingdust/lazyyyyy/forge/core/faster_module/BootstrapHooks.class")
        //                .readAllBytes());
        instrumentation.addTransformer(new ResolverClassTransformer(), true);
        instrumentation.retransformClasses(Class.forName("java.lang.module.Resolver"));
    }
}
