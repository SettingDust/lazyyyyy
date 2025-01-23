package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @Accessor
    @Mutable
    void setLayers(List<RenderLayer<?, ?>> layers);

    @Accessor
    List<RenderLayer<?, ?>> getLayers();
}
