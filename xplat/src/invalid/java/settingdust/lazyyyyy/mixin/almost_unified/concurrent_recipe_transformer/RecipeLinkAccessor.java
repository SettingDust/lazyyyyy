package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.recipe.RecipeLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RecipeLink.class, remap = false)
public interface RecipeLinkAccessor {
    @Invoker
    void callUpdateDuplicateLink(RecipeLink.DuplicateLink duplicateLink);
}
