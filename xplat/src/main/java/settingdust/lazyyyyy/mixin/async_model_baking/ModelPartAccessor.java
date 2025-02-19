package settingdust.lazyyyyy.mixin.async_model_baking;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor
    Map<String, ModelPart> getChildren();

    @Accessor
    @Mutable
    void setChildren(Map<String, ModelPart> value);
}
