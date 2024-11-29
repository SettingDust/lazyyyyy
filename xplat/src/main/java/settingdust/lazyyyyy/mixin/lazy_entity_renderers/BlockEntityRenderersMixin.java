package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.minecraft.LazyBlockEntityRenderer;

@Mixin(BlockEntityRenderers.class)
public class BlockEntityRenderersMixin {
    @WrapOperation(
        method = {"method_32145", "m_257086_"},
        remap = false,
        at = @At(
            value = "INVOKE",
            remap = true,
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;create(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;"
        )
    )
    private static <T extends BlockEntity> BlockEntityRenderer<T> lazyyyyy$lazyCreateRenderer(
        final BlockEntityRendererProvider<T> instance,
        final BlockEntityRendererProvider.Context context,
        final Operation<BlockEntityRenderer<T>> original
    ) {
        return new LazyBlockEntityRenderer<>(context, () -> original.call(instance, context));
    }
}
