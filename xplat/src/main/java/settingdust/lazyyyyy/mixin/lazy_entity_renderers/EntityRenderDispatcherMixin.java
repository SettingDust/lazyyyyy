package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.minecraft.LazyEntityRenderDispatcher;

import java.util.Collections;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements LazyEntityRenderDispatcher {
    @Unique
    public Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$renderers = Collections.emptyMap();

    @Unique
    public Map<String, EntityRenderer<? extends Player>> lazyyyyy$playerRenderers = Collections.emptyMap();


    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createEntityRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"
        )
    )
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$initLazyEntityRenderers(final Map<EntityType<?>, EntityRenderer<?>> original) {
        lazyyyyy$renderers = new Reference2ReferenceOpenHashMap<>(original);
        return original;
    }

    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"
        )
    )
    private Map<String, EntityRenderer<? extends Player>> lazyyyyy$initLazyPlayerRenderers(final Map<String, EntityRenderer<? extends Player>> original) {
        lazyyyyy$playerRenderers = new Reference2ReferenceOpenHashMap<>(original);
        return original;
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
    public @NotNull Map<EntityType<?>, EntityRenderer<?>> getLazyyyyy$renderers() {
        return lazyyyyy$renderers;
    }

    @Override
    public @NotNull Map<String, EntityRenderer<? extends Player>> getLazyyyyy$playerRenderers() {
        return lazyyyyy$playerRenderers;
    }
}
