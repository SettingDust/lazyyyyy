package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.recipe.RecipeTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import settingdust.lazyyyyy.almost_unified.ConcurrentRecipeTransformerKt;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mixin(value = RecipeTransformer.class, remap = false)
public abstract class RecipeTransformerMixin {
    /**
     * @author SettingDust
     * @reason Lazyyyyy Concurrent
     */
    @Overwrite
    private Set<RecipeLink.DuplicateLink> handleDuplicates(
        final Collection<RecipeLink> recipeLinks,
        final List<RecipeLink> linksToCompare
    ) {
        var links = ConcurrentRecipeTransformerKt.concurrentHandleDuplicates(
            (RecipeTransformer) (Object) this,
            recipeLinks,
            linksToCompare
        );
        ConcurrentRecipeTransformerKt.getRecipesToDuplicates().clear();
        return links;
    }
}
