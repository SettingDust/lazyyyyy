package settingdust.lazyyyyy.mixin.game.v1_20.pack_resources_cache;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;
import settingdust.lazyyyyy.game.util.pack_resources_cache.HashManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Shadow
    @Final
    private File file;

    @Unique
    private PackCache lazyyyyy$cache;
    @Unique
    private FileSystem lazyyyyy$fs;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final CallbackInfo ci) throws IOException {
        lazyyyyy$fs = FileSystems.newFileSystem(file.toPath());
        lazyyyyy$cache = new PackCache(lazyyyyy$fs.getPath(""), (PackResources) this);
    }

    @Inject(method = "close", remap = false, at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) {
        if (lazyyyyy$fs != null) {
            try {
                lazyyyyy$fs.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public @NotNull PackCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }

    @Override
    public byte[] lazyyyyy$getHash() {
        return HashManager.INSTANCE.getFileHash(file.toPath());
    }
}
