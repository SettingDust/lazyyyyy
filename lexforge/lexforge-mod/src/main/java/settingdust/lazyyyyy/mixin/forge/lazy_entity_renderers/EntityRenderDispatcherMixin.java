package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.forge.LazyEntityRenderersKt;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderers:Ljava/util/Map;"
        )
    )
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$renderers$filterOutLazyRenderers(final Map<EntityType<?>, EntityRenderer<?>> original) {
        return LazyEntityRenderersKt.filterLazyRenderers(original);
    }

    @ModifyExpressionValue(
        method = "onResourceManagerReload",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;playerRenderers:Ljava/util/Map;"
        )
    )
    private Map<EntityType<?>, EntityRenderer<?>> lazyyyyy$playerRenderers$filterOutLazyRenderers(final Map<EntityType<?>, EntityRenderer<?>> original) {
        return LazyEntityRenderersKt.filterLazyRenderers(original);
    }
}
