package settingdust.lazyyyyy.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingPackResources;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCache;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.VanillaPackResourcesCache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(VanillaPackResources.class)
public class VanillaPackResourcesMixin implements CachingPackResources {
    @Unique
    private VanillaPackResourcesCache lazyyyyy$cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(
        BuiltInMetadata metadata,
        Set<String> namespaces,
        List<Path> rootPaths,
        Map<PackType, List<Path>> pathsForType,
        final CallbackInfo ci
    ) {
        lazyyyyy$cache = new VanillaPackResourcesCache((PackResources) this, rootPaths, pathsForType);
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) throws IOException {
        lazyyyyy$cache.close();
    }

    @Override
    public @NotNull PackResourcesCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
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
        final String namespace,
        final String prefix,
        final PackResources.ResourceOutput resourceOutput,
        final Operation<Void> original
    ) {
        lazyyyyy$cache.listResources(packType, namespace, prefix, resourceOutput);
    }
}
