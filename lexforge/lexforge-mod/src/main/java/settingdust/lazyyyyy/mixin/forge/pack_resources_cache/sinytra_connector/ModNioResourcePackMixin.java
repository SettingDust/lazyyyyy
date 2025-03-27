package settingdust.lazyyyyy.mixin.forge.pack_resources_cache.sinytra_connector;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.minecraft.CachingPackResources;

import java.io.InputStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@IfModLoaded("fabric_resource_loader_v0")
@Mixin(ModNioResourcePack.class)
public abstract class ModNioResourcePackMixin implements CachingPackResources {
    @Redirect(
        method = "openFile",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/fabric/impl/resource/loader/ModNioResourcePack;resolve([Ljava/lang/String;)Ljava/nio/file/Path;"
        )
    )
    private Path lazyyyyy$removeOriginalLogic(final ModNioResourcePack instance, final String[] strings) {
        return null;
    }

    @Redirect(
        method = "openFile",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"
        )
    )
    private boolean lazyyyyy$removeOriginalLogic(final Path path, final LinkOption[] linkOptions) {
        return false;
    }

    @Inject(method = "openFile", remap = false, at = @At("HEAD"), cancellable = true)
    private void lazyyyyy$openFileFromCache(
        final String filename,
        final CallbackInfoReturnable<IoSupplier<InputStream>> cir
    ) {
        var result = getLazyyyyy$cache().getResource(filename);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
