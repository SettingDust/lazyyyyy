package settingdust.lazyyyyy.mixin.toomanyplayers.async_networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.feytox.toomanyplayers.TooManyPlayers;
import settingdust.lazyyyyy.util.AsyncNetworkingKt;

@IfModLoaded("toomanyplayers")
@Mixin(value = TooManyPlayers.class, remap = false)
public class TooManyPlayersMixin {
    @WrapOperation(
        method = "onInitialize",
        at = @At(value = "INVOKE", target = "Lru/feytox/toomanyplayers/OnlineWhitelist;reloadWhitelist()Z")
    )
    private boolean lazyyyyy$asyncList(final Operation<Boolean> original) {
        AsyncNetworkingKt.executeOffThread(original::call);
        return false;
    }
}
