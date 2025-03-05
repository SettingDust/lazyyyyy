package settingdust.lazyyyyy.mixin.lazy_entity_renderers.entity_model_features;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.util.fastutil_wrappers.WrappedObject2IntOpenHashMap;
import traben.entity_model_features.EMFManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@IfModLoaded("entity_model_features")
@Mixin(EMFManager.class)
public class EMFManagerMixin {


    @Shadow(remap = false)
    @Final
    @Mutable
    private Object2IntOpenHashMap<ModelLayerLocation> amountOfLayerAttempts;

    @WrapOperation(
        method = "<init>",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Ltraben/entity_model_features/EMFManager;amountOfLayerAttempts:Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;"
        )
    )
    private void lazyyyyy$syncMap(
        final EMFManager instance,
        final Object2IntOpenHashMap<ModelLayerLocation> value,
        final Operation<Void> original
    ) {
        original.call(instance, new WrappedObject2IntOpenHashMap<>(Object2IntMaps.synchronize(value)));
    }

    @WrapOperation(
        method = "<init>",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Ltraben/entity_model_features/EMFManager;loadingExceptions:Ljava/util/List;"
        )
    )
    private void lazyyyyy$syncMap(final EMFManager instance, final List<Exception> value, final Operation<Void> original) {
        original.call(instance, new CopyOnWriteArrayList<>(value));
    }
}
