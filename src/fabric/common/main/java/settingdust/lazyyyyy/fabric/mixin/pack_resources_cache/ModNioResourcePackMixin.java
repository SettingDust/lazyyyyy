package settingdust.lazyyyyy.fabric.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheCore;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;
import settingdust.lazyyyyy.game.util.pack_resources_cache.HashManager;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mixin(ModNioResourcePack.class)
public class ModNioResourcePackMixin implements PackCacheHolder, PackCacheHashProvider {
    @Unique
    private PackCache lazyyyyy$cache;
    @Unique
    private Path lazyyyyy$filePath;

    @Dynamic
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final CallbackInfo ci, @Local(argsOnly = true) List<Path> paths) {
        lazyyyyy$cache = new PackCache((PackResources) this, paths);
        if (paths.size() == 1 && Files.isRegularFile(paths.get(0))) {
            lazyyyyy$filePath = paths.get(0);
        }
    }

    @WrapMethod(method = "openFile")
    private IoSupplier<InputStream> lazyyyyy$cache(String filename, Operation<IoSupplier<InputStream>> original) {
        var result = lazyyyyy$cache.getResource(filename);
        return result == null ? original.call(filename) : result;
    }

    @Override
    public byte[] lazyyyyy$getHash() {
        if (lazyyyyy$filePath == null) return null;
        return HashManager.INSTANCE.getFileHash(lazyyyyy$filePath);
    }

    @Override
    public @NotNull PackCacheCore getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }
}
