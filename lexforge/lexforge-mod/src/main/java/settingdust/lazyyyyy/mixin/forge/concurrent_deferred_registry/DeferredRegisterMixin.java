package settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.forge.minecraft.ConcurrentDeferredRegistryKt;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = DeferredRegister.class, remap = false)
public class DeferredRegisterMixin {
    @Redirect(method = "addEntries", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private <T> Set lazyyyyy$concurrentAddEntries(
        final Map<RegistryObject<T>, Supplier<? extends T>> instance,
        RegisterEvent event
    ) {
        ConcurrentDeferredRegistryKt.concurrentAddEntries((DeferredRegister<T>) (Object) this, event, instance);
        return Collections.emptySet();
    }
}
