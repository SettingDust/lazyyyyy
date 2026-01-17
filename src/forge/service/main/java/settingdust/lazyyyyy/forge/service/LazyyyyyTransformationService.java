package settingdust.lazyyyyy.forge.service;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import cpw.mods.niofs.union.UnionPath;
import net.lenni0451.reflect.stream.RStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.launch.MixinLaunchPlugin;
import settingdust.preloading_tricks.modlauncher.module_injector.ModuleReplacer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LazyyyyyTransformationService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");

    public LazyyyyyTransformationService() {
    }

    @NotNull
    @Override
    public String name() {
        return "Lazyyyyy";
    }

    @Override
    public void initialize(IEnvironment iEnvironment) {

    }

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> otherServices) {
        try {
            LOGGER.info("Replacing mixin in BOOT layer");
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            UnionPath rootPath = (UnionPath) Path.of(codeSource.getLocation().toURI());
            Path mixinJar = rootPath.getRoot().resolve("sponge-mixin.jar");
            ModuleReplacer.replace("org.spongepowered.mixin", mixinJar, IModuleLayerManager.Layer.BOOT);

            LOGGER.info("Replacing mixin service launch plugin");
            LaunchPluginHandler launchPluginHandler = RStream.of(Launcher.class)
                    .fields()
                    .by("launchPlugins")
                    .get(Launcher.INSTANCE);
            Map<String, ILaunchPluginService> plugins = RStream.of(LaunchPluginHandler.class)
                    .fields()
                    .by("plugins")
                    .get(launchPluginHandler);
            plugins.put("mixin", new MixinLaunchPlugin());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
