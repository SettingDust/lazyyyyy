package settingdust.lazyyyyy.forge.core;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;
import settingdust.lazyyyyy.forge.core.faster_mixin.hack.FasterMixinServiceWrapper;
import settingdust.lazyyyyy.forge.core.faster_module.FasterModuleInstrumentationHack;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

public class LazyyyyyHacksInjector implements ITransformationService {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Unsafe UNSAFE = uncheck(() -> {
        var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    });

    public LazyyyyyHacksInjector() {
        LOGGER.info("Constructing");
        try {
            try {
                attachAgent();
            } catch (Throwable t) {
                LazyyyyyHacksInjector.LOGGER.warn(
                    "Error attaching bytebuddy agent, the major optimization for module resolver won't work");
                LazyyyyyHacksInjector.LOGGER.debug(
                    "Error attaching bytebuddy agent, the major optimization for module resolver won't work",
                    t
                );
            }
            ClassLoaderInjector.injectBootstrap();
            FasterModuleInstrumentationHack.replaceModuleResolver();
            ClassLoaderInjector.injectMcBootstrap();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void attachAgent() throws
                                      ClassNotFoundException,
                                      NoSuchFieldException {

        Field ALLOW_ATTACH_SELF =
            Class.forName("sun.tools.attach.HotSpotVirtualMachine")
                 .getDeclaredField("ALLOW_ATTACH_SELF");
        Object allowAttachSelfBase = UNSAFE.staticFieldBase(ALLOW_ATTACH_SELF);
        long allowAttachSelfOffset = UNSAFE.staticFieldOffset(ALLOW_ATTACH_SELF);
        boolean defaultValue = UNSAFE.getBooleanVolatile(allowAttachSelfBase, allowAttachSelfOffset);
        UNSAFE.putBooleanVolatile(allowAttachSelfBase, allowAttachSelfOffset, true);
        ByteBuddyAgent.install();
        UNSAFE.putBooleanVolatile(allowAttachSelfBase, allowAttachSelfOffset, defaultValue);
    }

    @Override
    public @NotNull String name() {
        return "lazyyyyy";
    }

    @Override
    public void initialize(final IEnvironment environment) {
        // Run after Connector, MixinBooster. Forge builtin mixin isn't supporting "mixin.service"
        LOGGER.info("Initializing");
        try {
            var initServiceMethod = MixinService.class.getDeclaredMethod("initService");
            initServiceMethod.setAccessible(true);
            var getInstanceMethod = MixinService.class.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            var service = initServiceMethod.invoke(getInstanceMethod.invoke(null));
            FasterMixinServiceWrapper.wrapped = (MixinServiceModLauncher) service;
            System.setProperty(
                "mixin.service",
                "settingdust.lazyyyyy.forge.core.faster_mixin.hack.FasterMixinServiceWrapper"
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
