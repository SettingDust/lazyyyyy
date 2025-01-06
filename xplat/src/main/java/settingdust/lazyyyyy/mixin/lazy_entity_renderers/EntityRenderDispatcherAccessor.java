package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
    @Accessor
    Map<EntityType<?>, EntityRenderer<?>> getRenderers();

    @Accessor
    void setRenderers(Map<EntityType<?>, EntityRenderer<?>> renderers);

    @Accessor
    Map<String, EntityRenderer<? extends Player>> getPlayerRenderers();

    @Accessor
    void setPlayerRenderers(Map<String, EntityRenderer<? extends Player>> playerRenderers);
}
