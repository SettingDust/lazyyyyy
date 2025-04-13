package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.minecraft.LazyEntityRenderersForgeKt;

import java.util.Map;

/**
 * After {@link settingdust.lazyyyyy.mixin.lazy_entity_renderers.EntityRenderDispatcherMixin}
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 1001)
public class EntityRenderDispatcherMixin {
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;


    /**
     * Need to replace the field instead of modify the param since some mods may iterate the field
     * <a href="https://github.com/ochotonida/artifacts/blob/1.20.1/forge/src/main/java/artifacts/forge/ArtifactsForgeClient.java#L62">artifacts iterating</a>
     */
    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createEntityRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"
        )
    )
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$filterLazyEntityRenderers(final Map<EntityType<?>, EntityRenderer<?>> original) {
        return LazyEntityRenderersForgeKt.filterLazyRenderers(original);
    }

    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"
        )
    )
    private Map<String, EntityRenderer<? extends Player>> lazyyyyy$replaceWithDummyPlayerRenderers(
        final Map<String, EntityRenderer<? extends Player>> original,
        @Local EntityRendererProvider.Context context
    ) {
        return LazyEntityRenderersForgeKt.replaceWithDummyPlayer(original, context);
    }

    /**
     * Inject at the tail for executing after forge add layers event
     */
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$observeRenderers(final ResourceManager resourceManager, final CallbackInfo ci) {
        renderers = LazyEntityRenderersForgeKt.observeEntityRenderers(renderers);
        playerRenderers = LazyEntityRenderersForgeKt.observePlayerRenderers(playerRenderers);
    }
}
