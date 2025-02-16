package settingdust.lazyyyyy.almost_unified

import com.almostreliable.unified.recipe.RecipeLink
import com.almostreliable.unified.recipe.RecipeTransformer
import com.almostreliable.unified.utils.JsonCompare
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import settingdust.lazyyyyy.collect
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.filter
import settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer.RecipeLinkAccessor
import settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer.RecipeLink_DuplicateLinkAccessor
import settingdust.lazyyyyy.mixin.almost_unified.concurrent_recipe_transformer.RecipeTransformerAccessor
import java.util.concurrent.ConcurrentHashMap

val recipesToDuplicates = ConcurrentHashMap<RecipeLink, RecipeLink.DuplicateLink>()

fun RecipeTransformer.concurrentHandleDuplicates(
    recipeLinks: Collection<RecipeLink>,
    linksToCompare: List<RecipeLink>
): Set<RecipeLink.DuplicateLink> = runBlocking {
    val duplicates = ConcurrentHashMap.newKeySet<RecipeLink.DuplicateLink>()

    withContext(Dispatchers.IO) {
        recipeLinks.asFlow().concurrent()
            .filter {
                val duplicated =
                    handleDuplicate(it, linksToCompare) && it.hasDuplicateLink()
                duplicated
            }
            .collect { duplicates += it.duplicateLink }
    }

    ObjectOpenHashSet(duplicates)
}

fun RecipeTransformer.handleDuplicate(
    recipeLink: RecipeLink,
    linksToCompare: List<RecipeLink>
) = (this as RecipeTransformerAccessor).callHandleDuplicate(recipeLink, linksToCompare)

fun RecipeLink.updateDuplicateLink(duplicateLink: RecipeLink.DuplicateLink) =
    (this as RecipeLinkAccessor).callUpdateDuplicateLink(duplicateLink)

fun RecipeLinkDuplicateLink(master: RecipeLink) = RecipeLink_DuplicateLinkAccessor.construct(master)

fun RecipeLink.DuplicateLink.updateMaster(master: RecipeLink) =
    (this as RecipeLink_DuplicateLinkAccessor).callUpdateMaster(master)