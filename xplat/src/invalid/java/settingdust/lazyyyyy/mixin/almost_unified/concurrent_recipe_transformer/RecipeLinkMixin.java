package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.recipe.RecipeLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RecipeLink.class, remap = false)
public class RecipeLinkMixin {
    private static final ResourceLocation SHAPED = new ResourceLocation("minecraft", "crafting_shaped");

    @Redirect(method = "compare", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
    private static boolean lazyyyyy$fasterCompare(final String instance, final Object o, RecipeLink first) {
        return first.getType().equals(SHAPED);
    }

    @Redirect(
        method = "compare", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/resources/ResourceLocation;toString()Ljava/lang/String;"
    )
    )
    private static String lazyyyyy$avoidCall(final ResourceLocation instance) {return null;}

    @WrapOperation(
        method = "handleDuplicate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/almostreliable/unified/recipe/RecipeLink;getDuplicateLink()Lcom/almostreliable/unified/recipe/RecipeLink$DuplicateLink;"
        )
    )
    private RecipeLink.DuplicateLink lazyyyyy$concurrentGetDuplicateLink(
        final RecipeLink instance, final Operation<RecipeLink.DuplicateLink> original
    ) {
        synchronized (instance) {
            return original.call(instance);
        }
    }
    @WrapOperation(
        method = "handleDuplicate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/almostreliable/unified/recipe/RecipeLink;updateDuplicateLink(Lcom/almostreliable/unified/recipe/RecipeLink$DuplicateLink;)V"
        )
    )
    private void lazyyyyy$concurrentUpdateDuplicateLink(
        final RecipeLink instance, final RecipeLink.DuplicateLink duplicateLink, final Operation<Void> original
    ) {
        synchronized (instance) {
            original.call(instance, duplicateLink);
        }
    }
}
