package settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer;

import com.almostreliable.unified.recipe.RecipeLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RecipeLink.DuplicateLink.class, remap = false)
public interface RecipeLink_DuplicateLinkAccessor {
    @Invoker("<init>")
    static RecipeLink.DuplicateLink construct(RecipeLink master) {
        throw new AssertionError();
    }

    @Invoker
    void callUpdateMaster(RecipeLink master);
}
