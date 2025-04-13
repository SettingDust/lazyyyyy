package settingdust.lazyyyyy.mixin.forge.jitl.fix_login_crash;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.jitl.client.ClientLoginChecker;
import net.jitl.core.init.JITL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientLoginChecker.class, remap = false)
public class ClientLoginCheckerMixin {
    @WrapOperation(
        method = "onPlayerLogin",
        at = {
            @At(value = "INVOKE", target = "Lnet/jitl/core/helper/InternetHandler;isOnline()Z"),
            @At(value = "INVOKE", target = "Lnet/jitl/core/helper/InternetHandler;isUpdateAvailable()Z")
        }
    )
    private static boolean lazyyyyy$avoidCrashBooleans(final Operation<Boolean> original) {
        try {
            return original.call();
        } catch (Exception e) {
            return false;
        }
    }

    @WrapOperation(
        method = "onPlayerLogin",
        at = {
            @At(value = "INVOKE", target = "Lnet/jitl/core/helper/InternetHandler;getUpdateVersion()Ljava/lang/String;")
        }
    )
    private static String lazyyyyy$avoidCrash(final Operation<String> original) {
        try {
            return original.call();
        } catch (Exception e) {
            return JITL.MOD_VERSION;
        }
    }
}
