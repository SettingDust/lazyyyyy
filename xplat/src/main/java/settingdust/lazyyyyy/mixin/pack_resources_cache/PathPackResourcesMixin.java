package settingdust.lazyyyyy.mixin.pack_resources_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.minecraft.CachingPackResources;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PathPackResources.class)
public class PathPackResourcesMixin {
    @Shadow
    @Final
    private Path root;
    @Unique
    private CachingPackResources lazyyyyy$cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lazyyyyy$init(String string, Path path, boolean bl, final CallbackInfo ci) {
        lazyyyyy$cache = new CachingPackResources(path, (PackResources) this);
    }

    @WrapMethod(method = "getNamespaces")
    private Set<String> lazyyyyy$getNamespaces(
        final PackType packType,
        final Operation<Set<String>> original
    ) {
        return lazyyyyy$cache.getNamespaces(packType);
    }

    @Inject(
        method = "getRootResource",
        at = @At(
            value = "INVOKE",
            target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"
        ),
        cancellable = true
    )
    private void lazyyyyy$getRootResource(
        final String[] paths,
        final CallbackInfoReturnable<IoSupplier<InputStream>> cir,
        @Local Path path
    ) {
        cir.setReturnValue(lazyyyyy$cache.getResource(path));
    }

    @Redirect(
        method = "getResource(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/IoSupplier;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/PathPackResources;getResource(Lnet/minecraft/resources/ResourceLocation;Ljava/nio/file/Path;)Lnet/minecraft/server/packs/resources/IoSupplier;"
        )
    )
    @Nullable
    private IoSupplier<InputStream> lazyyyyy$getResource(final ResourceLocation resourceLocation, final Path path) {
        return lazyyyyy$cache.getResource(path);
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
}
