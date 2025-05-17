package settingdust.lazyyyyy.forge.core.faster_module;

import net.lenni0451.reflect.Agents;
import settingdust.lazyyyyy.forge.core.LazyyyyyHacksInjector;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class FasterModuleInstrumentationHack {
    /**
     * https://github.com/openjdk/jdk/pull/16818
     */
    public static void replaceModuleResolver() throws
                                               ClassNotFoundException,
                                               UnmodifiableClassException {
        Instrumentation instrumentation;
        try {
            instrumentation = Agents.getInstrumentation();
        } catch (Throwable t) {
            LazyyyyyHacksInjector.LOGGER.debug(
                "No dummy agent, the major optimization for module resolver won't work",
                t
            );
            return;
        }
        var transformer = new ResolverClassTransformer();
        instrumentation.addTransformer(transformer, true);
        instrumentation.retransformClasses(Class.forName("java.lang.module.Resolver"));
        instrumentation.removeTransformer(transformer);
    }
}
