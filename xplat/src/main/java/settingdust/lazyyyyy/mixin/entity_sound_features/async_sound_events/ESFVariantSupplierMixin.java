package settingdust.lazyyyyy.mixin.entity_sound_features.async_sound_events;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import traben.entity_sound_features.ESFVariantSupplier;

@Mixin(value = ESFVariantSupplier.class, remap = false)
public class ESFVariantSupplierMixin {
    @Redirect(
        method = "getOrNull",
        at = @At(value = "INVOKE", target = "Ltraben/entity_sound_features/ESF;logError(Ljava/lang/String;)V")
    )
    private static void lazyyyyy$logTheException(final String message, @Local Exception e) {
        ESFAccessor.getLOGGER().error("Failed to load sound variant", e);
    }
}
