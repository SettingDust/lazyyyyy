package settingdust.lazyyyyy.simply_swords

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.sweenus.simplyswords.SimplySwords
import java.nio.file.Path
import kotlin.io.path.bufferedReader

private val parentMapping = mapOf(
    "GemEffects" to "gem_effects.json5",
    "General" to "general.json5",
    "Loot" to "loot.json5",
    "RunicEffects" to "runic_effects.json5",
    "StatusEffects" to "status_effects.json5",
    "UniqueEffects" to "unique_effects.json5",
    "WeaponAttributes" to "weapon_attributes.json5"
)

fun readConfig(parent: String, path: Path): JsonObject? {
    val filename = parentMapping[parent] ?: return null
    try {
        return JsonParser.parseReader(path.resolve(filename).bufferedReader()).asJsonObject
    } catch (e: Exception) {
        SimplySwords.LOGGER.error("Failed to read config file $filename", e)
        return null
    }
}