package settingdust.lazyyyyy.mixin.simply_swords.faster_config;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.sweenus.simplyswords.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.simply_swords.FasterConfigKt;

import java.nio.file.LinkOption;
import java.nio.file.Path;

@Mixin(value = Config.class, remap = false)
public class ConfigMixin {
    @Redirect(
        method = "safeValueFetch",
        at = @At(
            value = "INVOKE",
            target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"
        )
    )
    private static boolean lazyyyyy$avoidExist(final Path provider, final LinkOption[] x) {
        return false;
    }

    @ModifyVariable(method = "safeValueFetch", ordinal = 0, at = @At(value = "STORE"))
    private static JsonObject lazyyyyy$readConfig(JsonObject original, String type, String parent, @Local Path path) {
        return FasterConfigKt.readConfig(parent, path);
    }
}
