package settingdust.lazyyyyy.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingPackResources;
import settingdust.lazyyyyy.minecraft.pack_resources_cache.GenericPackResourcesCache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public class PathPackResourcesMixin implements CachingPackResources {
    @Unique
    private GenericPackResourcesCache lazyyyyy$cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(String string, Path path, boolean bl, final CallbackInfo ci) {
        lazyyyyy$cache = new GenericPackResourcesCache(path, (PackResources) this);
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) throws IOException {
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
    public @NotNull GenericPackResourcesCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }
}
