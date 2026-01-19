package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(targets = "org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher$Resource")
public class ContainerHandleModLauncher$ResourceTransformer implements IHashProvider {
    @Shadow
    private Path path;

    @Override
    @Unique
    public byte[] lazyyyyy$getHash() throws IOException {
        return MixinCacheManager.getFileHash(path);
    }
}
