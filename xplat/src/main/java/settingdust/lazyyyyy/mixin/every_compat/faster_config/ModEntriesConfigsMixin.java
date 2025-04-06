package settingdust.lazyyyyy.mixin.every_compat.faster_config;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.mehvahdjukaar.every_compat.EveryCompat;
import net.mehvahdjukaar.every_compat.configs.ModEntriesConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded(EveryCompat.MOD_ID)
@Mixin(value = ModEntriesConfigs.class, remap = false)
public class ModEntriesConfigsMixin {
    @ModifyExpressionValue(
        method = "isTypeEnabled(Lnet/mehvahdjukaar/moonlight/api/set/BlockType;Ljava/lang/String;)Z",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private static Object lazyyyyy$avoidException(
        final Object original,
        @Cancellable final CallbackInfoReturnable<Boolean> cir
    ) {
        if (original == null) {
            cir.setReturnValue(true);
        }
        return original;
    }
}
