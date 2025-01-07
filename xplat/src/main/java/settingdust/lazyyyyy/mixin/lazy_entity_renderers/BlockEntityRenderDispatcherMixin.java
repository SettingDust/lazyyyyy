package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.LazyEntityRenderersKt;

import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Shadow private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers;

    /**
     * Inject at the tail for executing after forge add layers event
     */
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$observeRenderers(final ResourceManager resourceManager, final CallbackInfo ci) {
        renderers = LazyEntityRenderersKt.observeBlockEntityRenderers(renderers);
    }
}
