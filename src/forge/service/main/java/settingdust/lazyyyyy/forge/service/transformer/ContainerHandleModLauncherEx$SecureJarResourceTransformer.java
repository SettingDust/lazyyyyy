package settingdust.lazyyyyy.forge.service.transformer;

import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;
import settingdust.lazyyyyy.faster_mixin.resource_provider.IResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Mixin(targets = "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncherEx$SecureJarResource")
public class ContainerHandleModLauncherEx$SecureJarResourceTransformer implements IResourceProvider, IHashProvider {
    @Shadow
    private SecureJar jar;

    @Override
    @Unique
    public InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException {
        return Files.newInputStream(jar.getPath(resource));
    }

    @Override
    @Unique
    public byte[] lazyyyyy$getHash() throws IOException {
        return MixinCacheManager.getFileHash(jar.getPrimaryPath());
    }
}
