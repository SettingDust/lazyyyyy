package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.habitat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.schnappdragon.habitat.client.renderer.block.HabitatChestRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "mod.schnappdragon.habitat.common.item.HabitatChestItem$1$1")
public class HabitatChestItemMixin {
    @ModifyExpressionValue(
        method = "renderByItem",
        at = @At(
            value = "FIELD",
            remap = false,
            target = "Lmod/schnappdragon/habitat/client/renderer/block/HabitatChestRenderer;INSTANCE:Lmod/schnappdragon/habitat/client/renderer/block/HabitatChestRenderer;"
        )
    )
    private static HabitatChestRenderer lazyyyyy$safeInstance(HabitatChestRenderer original) {
        if (original == null) {
            Minecraft.getInstance().getBlockEntityRenderDispatcher()
                     .getRenderer(((EntityBlock) HabitatChestRenderer.block)
                         .newBlockEntity(BlockPos.ZERO, HabitatChestRenderer.block.defaultBlockState()));
        }
        return HabitatChestRenderer.INSTANCE;
    }
}
