package settingdust.lazyyyyy.mixin.game.v1_21.pack_resources_cache.dynamic_trees;

import com.dtteam.dynamictrees.treepack.TreePackResources;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCache;
import settingdust.lazyyyyy.game.pack_resources_cache.TreePackCacheLayout;
import settingdust.lazyyyyy.mixin.game.pack_resources_cache.PathPackResourcesMixin;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Mixin(TreePackResources.class)
public class TreePackResourcesMixin extends PathPackResourcesMixin {
    @Override
    protected void lazyyyyy$init(String string, Path path, boolean bl, final CallbackInfo ci) {
        super.lazyyyyy$init(string, path, bl, ci);
        lazyyyyy$cache = new PackCache(
                (PackResources) this,
                List.of(path),
                TreePackCacheLayout.INSTANCE
        );
    }

    // TreePackResources overrides listResources - need to wrap
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

    // TreePackResources overrides getNamespaces - need to wrap
    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
            final @Nullable PackType type,
            final Operation<Set<String>> original
    ) {
        return lazyyyyy$cache.getNamespaces(type);
    }
}
