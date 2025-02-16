package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(value = RecipeTransformer.class, remap = false)
public interface RecipeTransformerAccessor {
    @Invoker
    void callTransformRecipes(
        List<RecipeLink> recipeLinks,
        Map<ResourceLocation, JsonElement> allRecipes,
        @Nullable ClientRecipeTracker.RawBuilder tracker
    );

    @Invoker
    Optional<JsonObject> callHandleForgeConditionals(RecipeLink recipeLink);

    @Invoker
    boolean callHandleDuplicate(RecipeLink curRecipe, List<RecipeLink> recipes);
}
