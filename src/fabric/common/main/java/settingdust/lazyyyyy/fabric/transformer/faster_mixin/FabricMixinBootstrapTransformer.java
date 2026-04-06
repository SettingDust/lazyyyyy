package settingdust.lazyyyyy.fabric.transformer.faster_mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.launch.FabricMixinBootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.fabric.faster_mixin.ContainerHandleMod;

@Mixin(FabricMixinBootstrap.class)
public abstract class FabricMixinBootstrapTransformer {
    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/Mixins;addConfiguration(Ljava/lang/String;)V"
            )
    )
    private static void lazyyyyy$addConfigurationWithSource(String configFile, @Local ModContainerImpl mod) {
        Mixins.addConfiguration(configFile, new ContainerHandleMod(mod));
    }
}
