package settingdust.lazyyyyy.mixin.forge.concurrent_deferred_registry;

import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RegistryObject.class, remap = false)
public interface RegistryObjectAccessor {
    @Invoker
    void invokeUpdateReference(RegisterEvent event);
}
