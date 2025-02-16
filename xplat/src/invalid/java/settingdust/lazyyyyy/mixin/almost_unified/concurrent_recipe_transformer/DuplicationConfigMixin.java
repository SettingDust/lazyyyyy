package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.config.DuplicationConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = DuplicationConfig.class, remap = false)
public class DuplicationConfigMixin {

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, Boolean> ignoredRecipeTypesCache;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lcom/almostreliable/unified/config/DuplicationConfig;ignoredRecipeTypesCache:Ljava/util/Map;"
        )
    )
    private void lazyyyyy$concurrentCache(
        final DuplicationConfig instance,
        final Map<ResourceLocation, Boolean> value
    ) {
        ignoredRecipeTypesCache = new ConcurrentHashMap<>();
    }
}
