package settingdust.lazyyyyy.fabric.transformer.better_log4j_config.earlier_init;

import com.pixelstorm.better_log4j_config.BetterLog4jConfig;
import net.lenni0451.classtransform.annotations.CInline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BetterLog4jConfig.class)
public class BetterLog4jConfigTransformer {
    @CInline
    @Inject(method = "onPreLaunch", at = @At("HEAD"), cancellable = true)
    private void lazyyyyy$avoid(CallbackInfo ci) {
        ci.cancel();
    }
}
