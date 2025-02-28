package settingdust.lazyyyyy.mixin.forge.puzzlelib.lazy_model_baking_events;

import fuzs.puzzleslib.api.event.v1.core.ForgeEventInvokerRegistry;
import fuzs.puzzleslib.impl.client.event.ForgeClientEventInvokers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.forge.puzzlelib.LazyModelBakingEventsKt;

import java.util.function.BiConsumer;

@Mixin(ForgeClientEventInvokers.class)
public class ForgeClientEventInvokersMixin {
    @Redirect(
        method = "registerLoadingHandlers",
        remap = false,
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lfuzs/puzzleslib/api/event/v1/core/ForgeEventInvokerRegistry;register(Ljava/lang/Class;Ljava/lang/Class;Ljava/util/function/BiConsumer;)V"
        )
    )
    private static void lazyyyyy$modifyUnbakedModel$useFabricApi(
        final ForgeEventInvokerRegistry instance,
        final Class clazz,
        final Class event,
        final BiConsumer converter
    ) {
        LazyModelBakingEventsKt.fabricApiModifyUnbakedModelEventInvoker();
    }

    @Redirect(
        method = "registerLoadingHandlers",
        remap = false,
        at = @At(
            value = "INVOKE",
            ordinal = 1,
            target = "Lfuzs/puzzleslib/api/event/v1/core/ForgeEventInvokerRegistry;register(Ljava/lang/Class;Ljava/lang/Class;Ljava/util/function/BiConsumer;)V"
        )
    )
    private static void lazyyyyy$modifyBakedModel$useFabricApi(
        final ForgeEventInvokerRegistry instance,
        final Class clazz,
        final Class event,
        final BiConsumer converter
    ) {
        LazyModelBakingEventsKt.fabricApiModifyBakedModelEventInvoker();
    }

    @Redirect(
        method = "registerLoadingHandlers",
        remap = false,
        at = @At(
            value = "INVOKE",
            ordinal = 2,
            target = "Lfuzs/puzzleslib/api/event/v1/core/ForgeEventInvokerRegistry;register(Ljava/lang/Class;Ljava/lang/Class;Ljava/util/function/BiConsumer;)V"
        )
    )
    private static void lazyyyyy$additionalBakedModel$useFabricApi(
        final ForgeEventInvokerRegistry instance,
        final Class clazz,
        final Class event,
        final BiConsumer converter
    ) {
        LazyModelBakingEventsKt.fabricApiAdditionalBakedModelEventInvoker();
    }
}
