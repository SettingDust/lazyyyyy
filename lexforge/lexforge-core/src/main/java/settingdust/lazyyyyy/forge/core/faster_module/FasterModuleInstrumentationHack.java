package settingdust.lazyyyyy.forge.core.faster_module;

import net.bytebuddy.agent.ByteBuddyAgent;
import settingdust.lazyyyyy.forge.core.LazyyyyyHacksInjector;
import sun.misc.Unsafe;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandles;

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
                                               UnmodifiableClassException {
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
        instrumentation.addTransformer(new ResolverClassTransformer(), true);
        instrumentation.retransformClasses(Class.forName("java.lang.module.Resolver"));
    }
}
