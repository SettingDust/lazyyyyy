package settingdust.lazyyyyy.game.pack_resources_cache

data class PackCacheSnapshot(
    val files: MutableMap<String, String> = mutableMapOf(),
    val directoryToFiles: MutableMap<String, MutableMap<String, String>> = mutableMapOf(),
    val namespaces: MutableMap<String, Set<String>> = mutableMapOf()
)

