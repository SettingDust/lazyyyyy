package settingdust.lazyyyyy.mixin.forge.ice_and_fire.faster_model_loader;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.model.util.TabulaModelHandlerHelper;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@IfModLoaded(IceAndFire.MODID)
@Mixin(value = TabulaModelHandlerHelper.class, remap = false)
public abstract class TabulaModelHandlerHelperMixin {
    @Shadow
    private static InputStream getModelJsonStream(final String name, final InputStream file) {
        return null;
    }

    @Unique
    private static final ModContainer lazyyyyy$mod = ModList.get().getModContainerById(IceAndFire.MODID).orElseThrow();

    @Redirect(
        method = "loadTabulaModel",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$cancelOriginal(final ClassLoader instance, final String s) {return null;}

    @Redirect(
        method = "loadTabulaModel",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/alexthe666/iceandfire/client/model/util/TabulaModelHandlerHelper;getModelJsonStream(Ljava/lang/String;Ljava/io/InputStream;)Ljava/io/InputStream;"
        )
    )
    private static InputStream lazyyyyy$faster(final String name, final InputStream file) throws IOException {
        var path = lazyyyyy$mod.getModInfo().getOwningFile().getFile().findResource(name);
        return getModelJsonStream(name, Files.newInputStream(path));
    }
}
