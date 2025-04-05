package settingdust.lazyyyyy.mixin.forge.pack_resources_cache.dynamic_trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.FlatTreeResourcePack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.minecraft.pack_resources_cache.TreePackResourcesCache;
import settingdust.lazyyyyy.mixin.forge.pack_resources_cache.PathPackResourcesMixin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@IfModLoaded(DynamicTrees.MOD_ID)
@Mixin(FlatTreeResourcePack.class)
public abstract class FlatTreeResourcePackMixin extends PathPackResourcesMixin {
    @Override
    protected void lazyyyyy$init(
        final String packId,
        final boolean isBuiltin,
        final Path source,
        final CallbackInfo ci
    ) {
        lazyyyyy$cache = new TreePackResourcesCache(source, (TreeResourcePack) this);
    }

    @WrapMethod(
        method = "getNamespaces"
    )
    private Set<String> lazyyyyy$getNamespaces(final @Nullable PackType type, final Operation<Set<String>> original) {
        return lazyyyyy$cache.getNamespaces(type);
    }

    @WrapMethod(
        method = "getResource"
    )
    private IoSupplier<InputStream> lazyyyyy$getResource(
        final PackType packType,
        final ResourceLocation location,
        final Operation<IoSupplier<InputStream>> original
    ) {
        return lazyyyyy$cache.getResource(packType, location);
    }

    @WrapMethod(method = "listResources")
    private void lazyyyyy$listResources(
        final PackType packType,
        final String namespace,
        final String path,
        final PackResources.ResourceOutput resourceOutput,
        final Operation<Void> original
    ) {
        lazyyyyy$cache.listResources(packType, namespace, path, resourceOutput);
    }
}
