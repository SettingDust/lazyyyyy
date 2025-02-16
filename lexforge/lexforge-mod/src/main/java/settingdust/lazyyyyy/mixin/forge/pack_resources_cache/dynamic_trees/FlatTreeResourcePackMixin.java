package settingdust.lazyyyyy.mixin.forge.pack_resources_cache.dynamic_trees;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.FlatTreeResourcePack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.minecraft.TreePackResourcesCache;
import settingdust.lazyyyyy.mixin.forge.pack_resources_cache.PathPackResourcesMixin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(FlatTreeResourcePack.class)
public abstract class FlatTreeResourcePackMixin extends PathPackResourcesMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final String packId, final boolean isBuiltin, final Path source, final CallbackInfo ci) {
        lazyyyyy$cache = new TreePackResourcesCache(source, (TreeResourcePack) this);
    }

    @WrapMethod(
        method = "getNamespaces"
    )
    private Set<String> lazyyyyy$getNamespaces(final @Nullable PackType type, final Operation<Set<String>> original) {
        if (lazyyyyy$cache != null) {
            return lazyyyyy$cache.getNamespaces(type);
        } else {
            return original.call(type);
        }
    }

    @WrapMethod(
        method = "getResource"
    )
    private IoSupplier<InputStream> lazyyyyy$getRootResource(
        final PackType packType,
        final ResourceLocation location,
        final Operation<IoSupplier<InputStream>> original
    ) {
        if (lazyyyyy$cache != null) {
            return lazyyyyy$cache.getResource(packType, location);
        } else {
            return original.call(packType, location);
        }
    }

    @WrapMethod(method = "listResources")
    private void lazyyyyy$listResources(
        final PackType packType,
        final String string,
        final String string2,
        final PackResources.ResourceOutput resourceOutput,
        final Operation<Void> original
    ) {
        if (lazyyyyy$cache != null) {
            lazyyyyy$cache.listResources(packType, string, string2, resourceOutput);
        } else {
            original.call(packType, string, string2, resourceOutput);
        }
    }
}
