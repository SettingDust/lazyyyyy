package settingdust.lazyyyyy.minecraft.pack_resources_cache

import kotlinx.serialization.Serializable
import net.minecraft.server.packs.PackType

@Serializable
data class PackResourcesCacheData(
    val files: Map<String, String> = emptyMap(),
    val directoryToFiles: Map<String, Map<String, String>> = emptyMap(),
    var namespaces: Map<PackType, Set<String>> = emptyMap()
)
