package settingdust.lazyyyyy.forge.core.faster_mixin;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;
import settingdust.lazyyyyy.forge.core.faster_mixin.hack.FasterMixinServiceWrapper;

import java.util.List;
import java.util.Set;

public class FasterMixinConfigLoaderInjector implements ITransformationService {
    public static final Logger LOGGER = LogManager.getLogger();

    public FasterMixinConfigLoaderInjector() {
        LOGGER.info("Constructing");
        try {
            InstrumentationHack.inject();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String name() {
        return "lazyyyyy faster mixin config loader injector";
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
