package settingdust.lazyyyyy.mixin.forge.lazy_entity_renderers.curios;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.Curios;

@IfModLoaded(Curios.MODID)
@Mixin(Curios.ClientProxy.class)
public class Curios_ClientProxyMixin {
    @Redirect(
        method = "addLayers",
        remap = false,
        at = @At(value = "INVOKE", target = "Ltop/theillusivec4/curios/api/client/CuriosRendererRegistry;load()V")
    )
    private static void lazyyyyy$avoidLoad() {}
}
