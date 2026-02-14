package settingdust.lazyyyyy.mixin.game.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.DetectedVersion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(VanillaPackResources.class)
public class VanillaPackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Shadow
    @Final
    private List<Path> rootPaths;
    @Shadow
    @Final
    private Map<PackType, List<Path>> pathsForType;

    @Unique
    private PackCache lazyyyyy$cache;
    @Unique
    private static final byte[] lazyyyyy$hash =
        ByteBuffer.allocate(4).putInt(DetectedVersion.BUILT_IN.getDataVersion().getVersion()).array();

    @Dynamic
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(
        BuiltInMetadata metadata,
        Set<String> namespaces,
        List<Path> rootPaths,
        Map<PackType, List<Path>> pathsForType,
        final CallbackInfo ci
    ) {
        lazyyyyy$initCache();
    }

    @Unique
    private void lazyyyyy$initCache() {
        List<Path> roots = new ArrayList<>(this.rootPaths);
        for (List<Path> paths : this.pathsForType.values()) {
            for (Path path : paths) {
                Path parent = path.getParent();
                if (parent != null && !roots.contains(parent)) {
                    roots.add(parent);
                }
            }
        }
        lazyyyyy$cache = new PackCache((PackResources) this, roots);
    }

    @Inject(method = "close", remap = false, at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) {
        lazyyyyy$cache.close();
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

    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
        final PackType packType,
        final Operation<Set<String>> original
    ) {
        return lazyyyyy$cache.getNamespaces(packType);
    }

    @Override
    public @NotNull PackCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }

    @Override
    public byte[] lazyyyyy$getHash() {
        return lazyyyyy$hash;
    }
}
