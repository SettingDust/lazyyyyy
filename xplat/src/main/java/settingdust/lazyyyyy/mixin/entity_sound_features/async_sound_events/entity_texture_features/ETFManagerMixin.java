package settingdust.lazyyyyy.mixin.entity_sound_features.async_sound_events.entity_texture_features;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.util.fastutil_wrappers.WrappedObject2ReferenceOpenHashMap;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.texture_handlers.ETFDirectory;

@IfModLoaded("entity_texture_features")
@Mixin(ETFManager.class)
public class ETFManagerMixin {
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
