package settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry;

import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.forge.minecraft.ConcurrentDeferredRegistry;

@Mixin(value = RegistryObject.class, remap = false)
public class RegistryObjectMixin {
    @Inject(method = "get", at = @At("HEAD"))
    private <T> void lazyyyyy$joinIfRegistering(final CallbackInfoReturnable<T> cir) {
        ConcurrentDeferredRegistry.INSTANCE.join((RegistryObject) (Object) this);
    }
}
