package settingdust.lazyyyyy.forge.minecraft.pack_resources_cache

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCache
import settingdust.lazyyyyy.minecraft.pack_resources_cache.consumeFile
import settingdust.lazyyyyy.minecraft.pack_resources_cache.consumeResourceDirectory
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class TreePackResourcesCache(pack: TreeResourcePack, treePackTypeRoots: List<Path>) :
    PackResourcesCache(pack, treePackTypeRoots) {
    constructor(root: Path, pack: TreeResourcePack) : this(pack, listOf(root))

    private val allNamespaces = CompletableDeferred<MutableSet<String>>()

    init {
        scope.launch { loadCache() }
    }

    private suspend fun CoroutineScope.consumePackType(
        directory: Path,
        namespaces: MutableSet<String>
    ) {
        val strategy = CachingStrategy.PackRoot(directory, directory)
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, Deferred<String>>>()
        directory.listDirectoryEntries().asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                namespaces += path.name
                consumeResourceDirectory(path, directoryToFiles, strategy)
            } else {
                consumeFile(this, path, strategy)
            }
        }
        for ((path, files) in directoryToFiles) {
            this@TreePackResourcesCache.directoryToFiles[path]!!.complete(files.mapValues { it.value.await() })
        }
    }

    private suspend fun CoroutineScope.loadCache() =
        withContext(CoroutineName("Dynamic Trees pack cache #${pack.packId()}")) {
            val time = measureTime {
                val namespaces = ConcurrentHashMap.newKeySet<String>()
                roots.asFlow().concurrent().collect { consumePackType(it, namespaces) }
                allNamespaces.complete(namespaces)
                allCompleted.complete()
            }
            Lazyyyyy.logger.debug("Cache tree pack ${pack.packId()} in $time")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?) =
        if (allNamespaces.isCompleted) allNamespaces.getCompleted()
        else runBlocking { allNamespaces.await() }
}