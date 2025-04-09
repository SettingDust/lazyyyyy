package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.server.packs.PackType

@Serializable
data class PackResourcesCacheData(
    val roots: Map<@Contextual HashCode, PackResourcesCacheDataEntry> = emptyMap(),
    val namespaces: Map<PackType, Set<String>> = emptyMap()
)

@Serializable
data class PackResourcesCacheDataEntry(
    val files: MutableMap<String, String> = mutableMapOf(),
    val directoryToFiles: MutableMap<String, MutableMap<String, String>> = mutableMapOf(),
)