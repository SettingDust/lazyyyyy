package settingdust.lazyyyyy.mixin.forge.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.Lazyyyyy;
import settingdust.lazyyyyy.minecraft.CachingPackResources;
import settingdust.lazyyyyy.minecraft.PackResourcesCacheKt;
import settingdust.lazyyyyy.minecraft.SimplePackResourcesCache;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public abstract class PathPackResourcesMixin implements CachingPackResources {
    @Unique
    @Nullable
    protected SimplePackResourcesCache lazyyyyy$cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(final String packId, final boolean isBuiltin, final Path source, final CallbackInfo ci) {
        // For supporting mods extends PathPackResources
        // noinspection ConstantValue
        if (((Class<?>) getClass()) == PathPackResources.class) {
            lazyyyyy$cache = new SimplePackResourcesCache(source, (PackResources) this);
        } else {
            if (!PackResourcesCacheKt.getSupported().contains(getClass().getName()))
                Lazyyyyy.INSTANCE.getLogger().warn(
                    "Failed to cache pack {}({}) that isn't a PathPackResources. Please report this to lazyyyyy issue tracker https://github.com/SettingDust/lazyyyyy/issues",
                    packId,
                    getClass()
                );
        }
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
        if (lazyyyyy$cache != null) {
            return lazyyyyy$cache.getNamespaces(packType);
        } else {
            return original.call(instance, packType);
        }
    }

    @WrapMethod(
        method = "getRootResource"
    )
    private IoSupplier<InputStream> lazyyyyy$getRootResource(
        final String[] paths,
        final Operation<IoSupplier<InputStream>> original
    ) {
        if (lazyyyyy$cache != null) {
            return lazyyyyy$cache.getResource(lazyyyyy$cache.join(paths));
        } else {
            return original.call((Object) paths);
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

    @Override
    public @Nullable SimplePackResourcesCache getLazyyyyy$cache() {
        return lazyyyyy$cache;
    }
}
