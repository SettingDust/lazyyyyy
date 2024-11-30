package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.lazyyyyy.forge.LazyEntityRenderersKt;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    /**
     * Need to replace the field instead of modify the param since some mods may iterate the field
     * <a href="https://github.com/ochotonida/artifacts/blob/1.20.1/forge/src/main/java/artifacts/forge/ArtifactsForgeClient.java#L62">artifacts iterating</a>
     */
    @Inject(
        method = "onResourceManagerReload",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraftforge/fml/ModLoader;get()Lnet/minecraftforge/fml/ModLoader;"
        )
    )
    private void lazyyyyy$filterOutLazyRenderers(
        CallbackInfo ci,
        @Local EntityRendererProvider.Context context,
        @Share("renderers") LocalRef<Map<EntityType<?>, EntityRenderer<?>>> renderersRef,
        @Share("playerRenderers") LocalRef<Map<String, EntityRenderer<? extends Player>>> playerRenderersRef
    ) {
        renderersRef.set(renderers);
        playerRenderersRef.set(playerRenderers);
        renderers = LazyEntityRenderersKt.replaceWithDummyLivingEntity(renderers, context);
        playerRenderers = LazyEntityRenderersKt.replaceWithDummyPlayer(playerRenderers, context);
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void lazyyyyy$resetLazyRenderers(
        CallbackInfo ci,
        @Share("renderers") LocalRef<Map<EntityType<?>, EntityRenderer<?>>> renderersRef,
        @Share("playerRenderers") LocalRef<Map<String, EntityRenderer<? extends Player>>> playerRenderersRef
    ) {
        renderers = renderersRef.get();
        playerRenderers = playerRenderersRef.get();
    }
}
