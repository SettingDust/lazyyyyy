package settingdust.lazyyyyy.forge.minecraft.pack_resources_cache

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.GenericPackResourcesCache
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCache
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesLayout
import settingdust.lazyyyyy.minecraft.pack_resources_cache.consumeFile
import settingdust.lazyyyyy.minecraft.pack_resources_cache.consumeResourceDirectory
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class TreePackResourcesCache(pack: TreeResourcePack, treePackTypeRoots: List<Path>) :
    GenericPackResourcesCache(pack, treePackTypeRoots, TreePackLayout) {
    constructor(root: Path, pack: TreeResourcePack) : this(pack, listOf(root))
}

object TreePackLayout : PackResourcesLayout {
    override suspend fun cachePack(cache: PackResourcesCache): Map<PackType, Set<String>> = coroutineScope {
        val namespaces = ConcurrentHashMap.newKeySet<String>()
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
        val time = measureTime {
            cache.roots.asFlow().concurrent().collect { root ->
                consumePackType(cache, this, root, namespaces, directoryToFiles)
            }
        }
        for ((path, files) in directoryToFiles) {
            cache.directoryToFiles[path]!!.complete(files)
        }
        Lazyyyyy.logger.debug("Cache tree pack ${cache.pack.packId()} in $time")
        PackType.entries.associateWith { namespaces as Set<String> }
    }

    private suspend fun consumePackType(
        cache: PackResourcesCache,
        scope: CoroutineScope,
        directory: Path,
        namespaces: MutableSet<String>,
        directoryToFiles: MutableMap<String, MutableMap<Path, String>>
    ) {
        val strategy = CachingStrategy.PackRoot(directory, directory)
        directory.listDirectoryEntries().asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                namespaces += path.name
                cache.consumeResourceDirectory(path, directoryToFiles, strategy)
            } else {
                cache.consumeFile(scope, path, strategy)
            }
        }
    }
}
