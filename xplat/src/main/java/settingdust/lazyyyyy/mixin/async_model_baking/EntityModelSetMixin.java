package settingdust.lazyyyyy.mixin.async_model_baking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import settingdust.lazyyyyy.minecraft.AsyncModelPart;

@Mixin(EntityModelSet.class)
public class EntityModelSetMixin {
    @WrapMethod(method = "bakeLayer")
    private ModelPart lazyyyyyy$asyncBake(
        final ModelLayerLocation modelLayerLocation,
        final Operation<ModelPart> original
    ) {
        return new AsyncModelPart(modelLayerLocation, () -> original.call(modelLayerLocation));
    }
}
