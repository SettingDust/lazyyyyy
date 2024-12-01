package settingdust.lazyyyyy.mixin.pack_resources_cache;

import net.minecraft.server.packs.resources.SimpleReloadInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimpleReloadInstance.class)
public class SimpleReloadInstanceMixin {
//    @Inject(method = "create", at = @At("HEAD"))
//    private static void lazyyyyy$closeCache(CallbackInfoReturnable<?> cir) {
//        Lazyyyyy.INSTANCE.getLogger().info("Invalidating pack caches");
//        PackResourcesCache.INSTANCE.invalidate();
//    }
}
