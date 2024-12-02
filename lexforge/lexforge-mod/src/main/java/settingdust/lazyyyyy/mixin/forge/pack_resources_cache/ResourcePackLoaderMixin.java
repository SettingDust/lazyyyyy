package settingdust.lazyyyyy.mixin.forge.pack_resources_cache;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraftforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Mixin(ResourcePackLoader.class)
public class ResourcePackLoaderMixin {
    @ModifyExpressionValue(
        method = "createPackForMod",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/forgespi/locating/IModFile;getFilePath()Ljava/nio/file/Path;"
        )
    )
    private static Path lazyyyyy$correctPackSource(final Path original) throws IOException {
        return FileSystems.newFileSystem(original).getPath("");
    }
}
