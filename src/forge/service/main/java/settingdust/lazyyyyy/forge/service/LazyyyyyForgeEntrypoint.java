package settingdust.lazyyyyy.forge.service;

import cpw.mods.modlauncher.api.IModuleLayerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.lazyyyyy.config.LazyyyyyEarlyConfig;
import settingdust.lazyyyyy.faster_mixin.FasterMixinEntrypoint;
import settingdust.preloading_tricks.api.PreloadingEntrypoint;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleAccessor;
import settingdust.preloading_tricks.forgelike.module_injector.accessor.ModuleLayerAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.ModuleConfigurationCreator;
import settingdust.preloading_tricks.modlauncher.module_injector.ModuleInjector;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.LauncherAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleClassLoaderAccessor;
import settingdust.preloading_tricks.modlauncher.module_injector.accessor.ModuleLayerHandlerAccessor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LazyyyyyForgeEntrypoint implements PreloadingEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");

    private final Path rootPath;

    public LazyyyyyForgeEntrypoint() throws IOException {
        // Load configuration
        LazyyyyyEarlyConfig.instance().load();

        try {
            var codeSource = getClass().getProtectionDomain().getCodeSource();
            rootPath = Path.of(codeSource.getLocation().toURI());
            injectBoot();
            FasterMixinEntrypoint.init(getClass().getClassLoader());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectBoot() {
        try {
            var bootClassLoader = ModuleLayerHandlerAccessor.getModuleClassLoader(IModuleLayerManager.Layer.BOOT);
            var bootJars = Files.list(rootPath.resolve("boot"))
                    .filter(it -> it.getFileName().toString().endsWith(".jar"))
                    .toList();
            LOGGER.info("Injected {} jars into BOOT layer", bootJars.size());
            LOGGER.debug("Injected jars: {}", bootJars);
            var configuration = ModuleConfigurationCreator.createConfigurationFromPaths(
                    bootJars,
                    ModuleClassLoaderAccessor.getConfiguration(bootClassLoader)
            );
            var bootLayer = LauncherAccessor.getModuleLayer(IModuleLayerManager.Layer.BOOT);
            ModuleInjector.inject(
                    configuration,
                    bootClassLoader,
                    bootLayer
            );

            var bootNameToModule = ModuleLayerAccessor.getNameToModule(bootLayer);
            var serviceModules = LauncherAccessor.getModuleLayer(IModuleLayerManager.Layer.SERVICE).modules();
            for (final var module : configuration.modules()) {
                for (final var serviceModule : serviceModules) {
                    ModuleAccessor.implAddReads(serviceModule, bootNameToModule.get(module.name()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
