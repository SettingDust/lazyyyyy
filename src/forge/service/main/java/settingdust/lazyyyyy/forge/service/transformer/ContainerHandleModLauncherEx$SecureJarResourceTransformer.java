package settingdust.lazyyyyy.forge.service.transformer;

import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import settingdust.lazyyyyy.faster_mixin.IResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Mixin(targets = "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncherEx$SecureJarResource")
public class ContainerHandleModLauncherEx$SecureJarResourceTransformer implements IResourceProvider {
    @Shadow
    private SecureJar jar;

    @Override
    @Unique
    public InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException {
        return Files.newInputStream(jar.getPath(resource));
    }
}
