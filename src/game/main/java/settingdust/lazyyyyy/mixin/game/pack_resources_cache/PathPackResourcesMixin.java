package settingdust.lazyyyyy.mixin.game.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;
import settingdust.lazyyyyy.game.util.pack_resources_cache.HashManager;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public class PathPackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Unique
    private PackCache lazyyyyy$cache;
    @Unique
    private Path lazyyyyy$rootPath;

    @Dynamic
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(String string, Path path, boolean bl, final CallbackInfo ci) {
        lazyyyyy$rootPath = path;
        lazyyyyy$cache = new PackCache(path, (PackResources) this);
    }

    @Inject(method = "close", remap = false, at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) {
        lazyyyyy$cache.close();
    }

    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
        final PackType packType,
        final Operation<Set<String>> original
    ) {
        return lazyyyyy$cache.getNamespaces(packType);
    }

    @WrapMethod(method = "getRootResource")
    private IoSupplier<InputStream> lazyyyyy$getRootResource(
        final String[] strings,
        final Operation<IoSupplier<InputStream>> original
    ) {
        return lazyyyyy$cache.getResource(lazyyyyy$cache.join(strings));
    }

    @WrapMethod(method = "getResource(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/IoSupplier;")
    @Nullable
    private IoSupplier<InputStream> lazyyyyy$getResource(
        final PackType packType,
        final ResourceLocation resourceLocation,
        final Operation<IoSupplier<InputStream>> original
    ) {
        return lazyyyyy$cache.getResource(packType, resourceLocation);
    }

    @WrapMethod(method = "listResources")
    private void lazyyyyy$listResources(
        final PackType packType,
        final String string,
        final String string2,
        final PackResources.ResourceOutput resourceOutput,
        final Operation<Void> original
    ) {
        lazyyyyy$cache.listResources(packType, string, string2, resourceOutput);
    }

    @Override
    public @NotNull PackCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }

    @Override
    public byte[] lazyyyyy$getHash() {
        if (lazyyyyy$rootPath == null) {
            return null;
        }
        return HashManager.INSTANCE.getFileHash(lazyyyyy$rootPath);
    }
}
