package settingdust.lazyyyyy.mixin.lazy_entity_renderers;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import settingdust.lazyyyyy.minecraft.LazyBlockEntityRenderer;

import java.util.HashMap;
import java.util.Map;

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
        final Operation<BlockEntityRenderer<T>> original,
        @Local(argsOnly = true) BlockEntityType<T> type
    ) {
        return new LazyBlockEntityRenderer<>(type, context, () -> original.call(instance, context));
    }

    @ModifyReturnValue(method = "createEntityRenderers", at = @At("TAIL"))
    private static Map<BlockEntityType<?>, BlockEntityRenderer<?>> lazyyyyy$mutableRenderers(final Map<BlockEntityType<?>, BlockEntityRenderer<?>> original) {
        return new HashMap<>(original);
    }
}
