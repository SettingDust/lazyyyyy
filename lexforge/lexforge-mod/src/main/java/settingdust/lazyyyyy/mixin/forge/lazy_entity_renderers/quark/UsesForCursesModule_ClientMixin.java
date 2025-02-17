package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.quark;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.client.module.UsesForCursesModule;

@IfModLoaded("quark")
@Mixin(UsesForCursesModule.Client.class)
public class UsesForCursesModule_ClientMixin {
    @ModifyExpressionValue(
        method = "modelLayers",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lorg/violetmoon/zeta/client/event/load/ZAddModelLayers;getRenderer(Lnet/minecraft/world/entity/EntityType;)Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
        )
    )
    private LivingEntityRenderer<?, ?> lazyyyyy$avoidAddIfNull(
        final LivingEntityRenderer<?, ?> original,
        @Cancellable CallbackInfo ci
    ) {
        if (original == null) {
            ci.cancel();
        }
        return original;
    }
}
