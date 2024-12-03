package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.quark;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.quark.content.client.module.UsesForCursesModule;
import org.violetmoon.zeta.client.event.load.ZAddModelLayers;
import settingdust.lazyyyyy.minecraft.DummyLivingEntityRenderer;

@IfModLoaded("quark")
@Mixin(UsesForCursesModule.Client.class)
public class UsesForCursesModule_ClientMixin {
    @WrapMethod(method = "modelLayers", remap = false)
    private void lazyyyyy$avoidAddIfDummy(final ZAddModelLayers event, final Operation<Void> original) {
        var renderer = (EntityRenderer<?>) event.getRenderer(EntityType.ARMOR_STAND);
        if (renderer == null || renderer instanceof DummyLivingEntityRenderer) return;
        original.call(event);
    }
}
