package settingdust.lazyyyyy.forge.service.transformer.cache;

import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;

import java.io.IOException;

@Mixin(targets = "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncherEx$SecureJarResource")
public class ContainerHandleModLauncherEx$SecureJarResourceHashTransformer implements IHashProvider {
    @Shadow
    private SecureJar jar;

    @Override
    @Unique
    public byte[] lazyyyyy$getHash() throws IOException {
        return MixinCacheManager.getFileHash(jar.getPrimaryPath());
    }
}
