package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.minecraft.LazyEntityRendererKt;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow private Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    /**
     * Inject at the tail for executing after forge add layers event
     */
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$observeRenderers(final ResourceManager resourceManager, final CallbackInfo ci) {
        renderers = LazyEntityRendererKt.observeEntityRenderers(renderers);
        playerRenderers = LazyEntityRendererKt.observePlayerRenderers(playerRenderers);
    }
}
