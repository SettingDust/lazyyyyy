package settingdust.lazyyyyy.forge.service;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.lenni0451.reflect.stream.RStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.launch.MixinLaunchPlugin;
import settingdust.lazyyyyy.Lazyyyyy;
import settingdust.preloading_tricks.modlauncher.module_injector.ModuleReplacer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LazyyyyyTransformationService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");

    private final Path rootPath;

    public LazyyyyyTransformationService() {
        try {
            var codeSource = getClass().getProtectionDomain().getCodeSource();
            rootPath = Path.of(codeSource.getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public String name() {
        return Lazyyyyy.ID;
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) {
        replaceMixin();
    }

    private void replaceMixin() {
        LOGGER.info("Replacing mixin in BOOT layer");
        var mixinJar = rootPath.resolve("sponge-mixin.jar");
        ModuleReplacer.replace("org.spongepowered.mixin", mixinJar, IModuleLayerManager.Layer.BOOT);

        LOGGER.info("Replacing mixin service launch plugin");
        var launchPluginHandler = RStream.of(Launcher.class)
                .fields()
                .by("launchPlugins")
                .get(Launcher.INSTANCE);
        Map<String, ILaunchPluginService> plugins = RStream.of(LaunchPluginHandler.class)
                .fields()
                .by("plugins")
                .get(launchPluginHandler);
        plugins.put("mixin", new MixinLaunchPlugin());
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
