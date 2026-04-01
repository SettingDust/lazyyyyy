package settingdust.lazyyyyy.forge.game.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;
import settingdust.lazyyyyy.game.util.pack_resources_cache.FileSystemPathUtilKt;
import settingdust.lazyyyyy.game.util.pack_resources_cache.HashManager;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public class ForgePathPackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Unique
    protected PackCache lazyyyyy$cache;
    @Unique
    private Path lazyyyyy$filePath;

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void lazyyyyy$init(String packId, boolean isBuiltin, Path source, final CallbackInfo ci) {
        lazyyyyy$filePath = FileSystemPathUtilKt.getFileSystemPath(source);
        lazyyyyy$cache = new PackCache(source, (PackResources) this);
    }

    @Inject(method = "close", remap = false, at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) {
        lazyyyyy$cache.close();
    }

    @WrapOperation(
        method = "getNamespaces",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraftforge/resource/PathPackResources;getNamespacesFromDisk(Lnet/minecraft/server/packs/PackType;)Ljava/util/Set;"
        )
    )
    private Set<String> lazyyyyy$getNamespaces(
        final PathPackResources instance, final PackType packType, final Operation<Set<String>> original
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
        if (lazyyyyy$filePath == null) return null;
        return HashManager.INSTANCE.getFileHash(lazyyyyy$filePath);
    }
}
