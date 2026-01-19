package settingdust.lazyyyyy.faster_mixin.transformer.resource_provider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import settingdust.lazyyyyy.faster_mixin.resource_provider.IResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(targets = "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher$Resource")
public class ContainerHandleModLauncher$ResourceTransformer implements IResourceProvider {
    @Shadow
    private Path path;

    @Override
    @Unique
    public InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException {
        return Files.newInputStream(path.resolve(resource));
    }
}
