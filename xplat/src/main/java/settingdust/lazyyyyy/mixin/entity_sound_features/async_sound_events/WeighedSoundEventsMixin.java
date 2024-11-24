package settingdust.lazyyyyy.mixin.entity_sound_features.async_sound_events;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kotlinx.coroutines.Job;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import settingdust.lazyyyyy.entity_sound_features.AsyncSoundEventsKt;
import traben.entity_sound_features.ESFVariantSupplier;
import traben.entity_sound_features.mixin.MixinWeighedSoundEvents;

@Mixin(value = WeighedSoundEvents.class, priority = 1001, remap = false)
public class WeighedSoundEventsMixin {
    @Shadow
    @Dynamic(mixin = MixinWeighedSoundEvents.class)
    private ESFVariantSupplier esf$variator;

    @Unique
    private Job lazyyyyy$loading;

    @Dynamic(mixin = MixinWeighedSoundEvents.class)
    @TargetHandler(
        mixin = "traben.entity_sound_features.mixin.MixinWeighedSoundEvents",
        name = "esf$init",
        prefix = "handler"
    )
    @WrapOperation(
        method = "@MixinSquared:Handler",
        at = @At(
            value = "INVOKE",
            target = "Ltraben/entity_sound_features/ESFVariantSupplier;getOrNull(Lnet/minecraft/resources/ResourceLocation;)Ltraben/entity_sound_features/ESFVariantSupplier;"
        )
    )
    private ESFVariantSupplier lazyyyyy$asyncGetVariantSupplier(
        ResourceLocation soundEventResource,
        Operation<ESFVariantSupplier> original
    ) {
        lazyyyyy$loading = AsyncSoundEventsKt.asyncGetVariantSupplier(
            () -> original.call(soundEventResource),
            (it) -> {esf$variator = it;}
        );
        return null;
    }

    @Dynamic(mixin = MixinWeighedSoundEvents.class)
    @TargetHandler(
        mixin = "traben.entity_sound_features.mixin.MixinWeighedSoundEvents",
        name = "esf$soundModify",
        prefix = "handler"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "HEAD"))
    private void lazyyyyy$awaitTheJob(
        RandomSource randomSource,
        CallbackInfoReturnable<Sound> originalCir,
        CallbackInfo ci
    ) {
        AsyncSoundEventsKt.joinBlocking(lazyyyyy$loading);
    }
}
