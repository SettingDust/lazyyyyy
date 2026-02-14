package settingdust.lazyyyyy.mixin.game.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHashProvider;
import settingdust.lazyyyyy.game.pack_resources_cache.PackCacheHolder;

import java.io.InputStream;
import java.util.Set;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements PackCacheHolder, PackCacheHashProvider {
    @Dynamic
    @Inject(method = "close", remap = false, at = @At("TAIL"))
    private void lazyyyyy$close(final CallbackInfo ci) {
        getLazyyyyy$cache().close();
    }

    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
            final PackType packType,
            final Operation<Set<String>> original
    ) {
        return getLazyyyyy$cache().getNamespaces(packType);
    }

    @WrapMethod(method = "getResource(Ljava/lang/String;)Lnet/minecraft/server/packs/resources/IoSupplier;")
    @Nullable
    private IoSupplier<InputStream> lazyyyyy$getResource(
            final String string,
            final Operation<IoSupplier<InputStream>> original
    ) {
        return getLazyyyyy$cache().getResource(string);
    }

    @WrapMethod(method = "listResources")
    private void lazyyyyy$listResources(
            final PackType packType,
            final String namespace,
            final String prefix,
            final PackResources.ResourceOutput resourceOutput,
            final Operation<Void> original
    ) {
        getLazyyyyy$cache().listResources(packType, namespace, prefix, resourceOutput);
    }
}
