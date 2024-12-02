package settingdust.lazyyyyy.mixin.forge.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.CachingPackResources;
import settingdust.lazyyyyy.minecraft.PackResourcesCache;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public abstract class PathPackResourcesMixin implements CachingPackResources {
    @Unique
    private PackResourcesCache lazyyyyy$cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final String packId, final boolean isBuiltin, final Path source, final CallbackInfo ci) {
        lazyyyyy$cache = new PackResourcesCache(source, (PackResources) this);
    }

    @Redirect(
        method = "getNamespaces",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraftforge/resource/PathPackResources;getNamespacesFromDisk(Lnet/minecraft/server/packs/PackType;)Ljava/util/Set;"
        )
    )
    private Set<String> lazyyyyy$getNamespaces(
        final PathPackResources instance, final PackType packType
    ) {
        return lazyyyyy$cache.getNamespaces(packType);
    }

    @WrapMethod(
        method = "getRootResource"
    )
    private IoSupplier<InputStream> lazyyyyy$getRootResource(
        final String[] paths,
        final Operation<IoSupplier<InputStream>> original
    ) {
        return lazyyyyy$cache.getResource(lazyyyyy$cache.join(paths));
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
    public @NotNull PackResourcesCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }
}
