package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor<T extends Entity> {
    @Invoker
    int invokeGetSkyLightLevel(T entity, BlockPos blockPos);

    @Invoker
    int invokeGetBlockLightLevel(T entity, BlockPos blockPos);

    @Invoker
    boolean invokeShouldShowName(T entity);

    @Invoker
    void invokeRenderNameTag(
        T entity,
        Component component,
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        int i
    );
}
