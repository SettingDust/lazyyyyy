package settingdust.lazyyyyy.mixin.axiom.async_check_commercial;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.axiom.Authorization;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(value = Authorization.class, remap = false)
public class AuthorizationMixin {
    @WrapMethod(method = "checkCommercial")
    private static CompletableFuture<Boolean> lazyyyyy$async(final Operation<CompletableFuture<Boolean>> original) {
        return CompletableFuture.supplyAsync(() -> original.call().join());
    }
}
