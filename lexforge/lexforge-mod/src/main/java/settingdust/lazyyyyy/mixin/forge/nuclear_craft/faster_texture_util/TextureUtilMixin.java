package settingdust.lazyyyyy.mixin.forge.nuclear_craft.faster_texture_util;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import igentuman.nc.NuclearCraft;
import igentuman.nc.util.TextureUtil;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@IfModLoaded(NuclearCraft.MODID)
@Mixin(TextureUtil.class)
public class TextureUtilMixin {
    @Unique
    private static final ModContainer container = ModList.get().getModContainerById(NuclearCraft.MODID).orElseThrow();

    @Redirect(
        method = "getAverageColor",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$fastGet(final ClassLoader instance, final String path) throws IOException {
        return Files.newInputStream(container.getModInfo().getOwningFile().getFile().findResource(path));
    }
}
