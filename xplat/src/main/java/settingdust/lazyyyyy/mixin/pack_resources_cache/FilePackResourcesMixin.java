package settingdust.lazyyyyy.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingPackResources;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.GenericPackResourcesCache;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.HashablePackResources;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCacheManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Set;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements CachingPackResources, HashablePackResources {
    @Shadow
    @Final
    private File file;

    @Unique
    private GenericPackResourcesCache lazyyyyy$cache;
    @Unique
    private FileSystem lazyyyyy$fs;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final CallbackInfo ci) throws IOException {
        lazyyyyy$fs = FileSystems.newFileSystem(file.toPath());
        lazyyyyy$cache = new GenericPackResourcesCache(lazyyyyy$fs.getPath(""), (PackResources) this);
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

    @WrapMethod(method = "getResource(Ljava/lang/String;)Lnet/minecraft/server/packs/resources/IoSupplier;")
    @Nullable
    private IoSupplier<InputStream> lazyyyyy$getResource(
        final String string,
        final Operation<IoSupplier<InputStream>> original
    ) {
        return lazyyyyy$cache.getResource(string);
    }

    @WrapMethod(method = "listResources")
    private void lazyyyyy$listResources(
        final PackType packType,
        final String namespace,
        final String prefix,
        final PackResources.ResourceOutput resourceOutput,
        final Operation<Void> original
    ) {
        lazyyyyy$cache.listResources(packType, namespace, prefix, resourceOutput);
    }

    @Override
    public @NotNull GenericPackResourcesCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }

    @Override
    public byte @Nullable [] lazyyyyy$getHash() {
        return PackResourcesCacheManager.INSTANCE.getFileHash(file);
    }
}
