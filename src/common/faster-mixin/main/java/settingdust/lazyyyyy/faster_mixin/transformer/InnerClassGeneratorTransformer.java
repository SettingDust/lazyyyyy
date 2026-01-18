package settingdust.lazyyyyy.faster_mixin.transformer;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.InnerClassGenerator")
public class InnerClassGeneratorTransformer {
    @Redirect(
            method = "getUniqueReference",
            at = @At(value = "INVOKE", target = "Ljava/util/UUID;randomUUID()Ljava/util/UUID;"))
    private static UUID lazyyyyy$stableUniqueReference(@Local(argsOnly = true) String originalName) {
        return UUID.nameUUIDFromBytes(originalName.getBytes(StandardCharsets.UTF_8));
    }
}
