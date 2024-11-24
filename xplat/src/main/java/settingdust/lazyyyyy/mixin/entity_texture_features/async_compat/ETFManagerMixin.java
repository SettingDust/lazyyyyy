package settingdust.lazyyyyy.mixin.entity_texture_features.async_compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.entity_sound_features.WrappedObject2ReferenceOpenHashMap;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;

@Mixin(ETFManager.class)
public class ETFManagerMixin {
    @Shadow(remap = false)
    @Final
    @Mutable
    public Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @NotNull ETFDirectory> ETF_DIRECTORY_CACHE;

    @WrapOperation(
        method = "<init>",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Ltraben/entity_texture_features/features/ETFManager;ETF_DIRECTORY_CACHE:Lit/unimi/dsi/fastutil/objects/Object2ReferenceOpenHashMap;"
        )
    )
    private void lazyyyyy$syncMap(
        final ETFManager instance,
        final Object2ReferenceOpenHashMap<@NotNull ResourceLocation, @NotNull ETFDirectory> value,
        final Operation<Void> original
    ) {
        original.call(instance, new WrappedObject2ReferenceOpenHashMap<>(Object2ReferenceMaps.synchronize(value)));
    }
}
