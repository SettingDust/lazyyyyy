package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.supplementaries;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.SignPostBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SignPostBlockTileRenderer.class)
public class SignPostBlockTileRendererMixin {
    @Shadow(remap = false)
    @Final
    public static Map<WoodType, BakedModel> MODELS;

    @Shadow(remap = false) private static ModelBlockRenderer renderer;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void lazyyyyy$init(final CallbackInfo ci) {

        ModelManager manager = Minecraft.getInstance().getModelManager();
        MODELS.clear();

        for (Map.Entry<WoodType, ResourceLocation> e : ClientRegistry.SIGN_POST_MODELS.get().entrySet()) {
            MODELS.put(e.getKey(), ClientHelper.getModel(manager, e.getValue()));
        }

        renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    }
}
