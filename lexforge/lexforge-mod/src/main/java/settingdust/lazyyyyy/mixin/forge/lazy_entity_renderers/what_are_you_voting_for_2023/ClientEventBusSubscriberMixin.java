package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.what_are_you_voting_for_2023;

import com.alexander.whatareyouvotingfor.WhatAreYouVotingFor;
import com.alexander.whatareyouvotingfor.util.ClientEventBusSubscriber;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded(WhatAreYouVotingFor.MODID)
@Mixin(ClientEventBusSubscriber.class)
public class ClientEventBusSubscriberMixin {
    @WrapOperation(
        method = "addLayers",
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getModel()Lnet/minecraft/client/model/EntityModel;"
        )
    )
    private static EntityModel<?> lazyyyyy$addNullCheck(
        final LivingEntityRenderer<?, ?> instance, final Operation<EntityModel<?>> original
    ) {
        if (instance == null) return null;
        else return original.call(instance);
    }
}
