package settingdust.lazyyyyy.mixin.forge.pack_resources_cache;

import com.google.common.hash.HashCode;
import net.minecraft.server.packs.PathPackResources;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.PlatformService;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.HashablePackResources;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCacheManager;

import java.nio.file.Path;

@Mixin(value = PathPackResources.class, priority = 999)
public class PathPackResourcesMixin implements HashablePackResources {
    @Unique
    protected Path lazyyyyy$filePath = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void lazyyyyy$init(final String string, final Path path, final boolean bl, final CallbackInfo ci) {
        lazyyyyy$filePath = PlatformService.Companion.getFileSystemPath(path);
    }

    @Override
    public @Nullable HashCode lazyyyyy$getHash() {
        if (lazyyyyy$filePath != null) {
            return PackResourcesCacheManager.INSTANCE.getFileHash(lazyyyyy$filePath);
        }
        return null;
    }
}
