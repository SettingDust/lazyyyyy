package settingdust.lazyyyyy.mixin.forge.prismlib.faster_colors_loader;

import com.anthonyhilyard.prism.util.WebColors;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@IfModLoaded("prism")
@Mixin(WebColors.class)
public class WebColorsMixin {
    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$fasterFindResource(final ClassLoader instance, final String name) throws
                                                                                                          IOException {
        return Files.newInputStream(LoadingModList.get().getModFileById("prism").getFile().findResource(name));
    }
}
