package settingdust.lazyyyyy.minecraft.pack_resources_cache

import kotlinx.serialization.Serializable
import net.minecraft.server.packs.PackType
import java.nio.file.Path

@Serializable
data class PackResourcesCacheData(
    val files: Map<String, Path> = emptyMap(),
    val directoryToFiles: Map<String, Map<Path, String>> = emptyMap(),
    var namespaces: Map<PackType, Set<String>> = emptyMap()
)
