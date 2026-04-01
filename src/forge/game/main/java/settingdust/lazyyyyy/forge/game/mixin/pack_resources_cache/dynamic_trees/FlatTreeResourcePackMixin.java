package settingdust.lazyyyyy.forge.game.mixin.pack_resources_cache.dynamic_trees;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.game.mixin.pack_resources_cache.ForgePathPackResourcesMixin;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.TreePackCacheLayout;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Mixin(FlatTreeResourcePack.class)
public abstract class FlatTreeResourcePackMixin extends ForgePathPackResourcesMixin {
    @Override
    protected void lazyyyyy$init(
            final String packId,
            final boolean isBuiltin,
            final Path source,
            final CallbackInfo ci
    ) {
        lazyyyyy$cache = new PackCache(
                (TreeResourcePack) this,
                List.of(source),
                TreePackCacheLayout.INSTANCE
        );
    }

    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
            final @Nullable PackType type,
            final Operation<Set<String>> original
    ) {
        return lazyyyyy$cache.getNamespaces(type);
    }

    @WrapMethod(method = "getResource")
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
