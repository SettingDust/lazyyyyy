package settingdust.lazyyyyy.mixin.game.v1_21.pack_resources_cache;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Shadow
    @Final
    private FilePackResources.SharedZipFileAccess zipFileAccess;
    @Shadow
    @Final
    private String prefix;

    @Unique
    private PackCache lazyyyyy$cache;
    @Unique
    private FileSystem lazyyyyy$fs;
    @Unique
    private File lazyyyyy$file;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(
        final PackLocationInfo location,
        final FilePackResources.SharedZipFileAccess zipFileAccess,
        final String prefix,
        final CallbackInfo ci
    ) throws IOException {
        lazyyyyy$file = this.zipFileAccess.file;
        lazyyyyy$fs = FileSystems.newFileSystem(lazyyyyy$file.toPath());
        Path root = prefix.isEmpty() ? lazyyyyy$fs.getPath("") : lazyyyyy$fs.getPath(prefix);
        List<Path> roots = Files.exists(root) ? List.of(root) : List.of();
        lazyyyyy$cache = new PackCache((PackResources) this, roots);
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
        if (lazyyyyy$file == null) {
            return null;
        }
        byte[] fileHash = HashManager.INSTANCE.getFileHash(lazyyyyy$file.toPath());
        if (prefix.isEmpty()) {
            return fileHash;
        }
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] combined = Arrays.copyOf(fileHash, fileHash.length + prefixBytes.length);
        System.arraycopy(prefixBytes, 0, combined, fileHash.length, prefixBytes.length);
        return combined;
    }
}
