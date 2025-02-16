package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.minecraft.LazyEntityRenderDispatcher;
import settingdust.lazyyyyy.forge.minecraft.LazyEntityRenderersForgeKt;

import java.util.Collections;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements LazyEntityRenderDispatcher {
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Unique
    public Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$renderers = Collections.emptyMap();

    @Unique
    public Map<String, EntityRenderer<? extends Player>> lazyyyyy$playerRenderers = Collections.emptyMap();


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
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$initLazyEntityRenderers(final Map<EntityType<?>, EntityRenderer<?>> original) {
        lazyyyyy$renderers = new Reference2ReferenceOpenHashMap<>(original);
        return LazyEntityRenderersForgeKt.filterLazyRenderers(original);
    }

    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"
        )
    )
    private Map<String, EntityRenderer<? extends Player>> lazyyyyy$initLazyPlayerRenderers(
        final Map<String, EntityRenderer<? extends Player>> original,
        @Local EntityRendererProvider.Context context
    ) {
        lazyyyyy$playerRenderers = new Reference2ReferenceOpenHashMap<>(original);
        return LazyEntityRenderersForgeKt.replaceWithDummyPlayer(original, context);
    }

    @Redirect(
        method = "getRenderer",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderers:Ljava/util/Map;"
        )
    )
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$useLazyRenderers(EntityRenderDispatcher instance) {
        return lazyyyyy$renderers;
    }

    @Redirect(
        method = "getRenderer",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;playerRenderers:Ljava/util/Map;"
        )
    )
    private Map<String, EntityRenderer<? extends Player>> lazyyyyy$useLazyPlayerRenderers(EntityRenderDispatcher instance) {
        return lazyyyyy$playerRenderers;
    }

    @Override
    public Map<EntityType<?>, EntityRenderer<?>> getLazyyyyy$renderers() {
        return lazyyyyy$renderers;
    }

    @Override
    public Map<String, EntityRenderer<? extends Player>> getLazyyyyy$playerRenderers() {
        return lazyyyyy$playerRenderers;
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
