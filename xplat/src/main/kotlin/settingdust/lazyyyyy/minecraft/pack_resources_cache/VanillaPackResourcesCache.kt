package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.DetectedVersion
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import settingdust.lazyyyyy.util.flatMap
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.measureTime

class VanillaPackResourcesCache(
    pack: PackResources,
    roots: List<Path>,
    private val pathsForType: Map<PackType, List<Path>>
) : PackResourcesCache(pack, roots) {
    companion object {
        val HASH = HashCode.fromInt(DetectedVersion.BUILT_IN.dataVersion.version)
    }

    init {
        scope.launch { loadCache() }
    }

    private suspend fun CoroutineScope.consumeRoot(root: Path) {
        val strategy = CachingStrategy.PackRoot(root, null)
        val blacklisted = packTypeByDirectory.keys + listOf("net", "com", "coremods", "META-INF", "minecraft", "realms")
        root.listDirectoryEntries().asFlow().concurrent().collect { path ->
            val relativePath = root.relativize(path)
            val firstPath = relativePath.firstOrNull()
            if (firstPath?.name in blacklisted) return@collect
            if (path.isDirectory()) {
                consumeRootDirectory(this, root, path, strategy)
            } else {
                consumeFile(this, root, path, strategy)
            }
        }
    }

    suspend fun PackResourcesCache.consumePackType(
        root: Path,
        strategy: CachingStrategy,
        directoryToFiles: MutableMap<String, MutableMap<Pair<Path, Path>, String>>
    ) {
        coroutineScope {
            val entries = root.listDirectoryEntries()
            entries.asFlow().concurrent().collect { path ->
                if (path.isDirectory()) {
                    consumeResourceDirectory(root, path, directoryToFiles, strategy)
                } else {
                    consumeFile(this, root, path, strategy)
                }
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun CoroutineScope.loadCache() =
        withContext(CoroutineName("Vanilla pack cache #${pack.packId()}")) {
            val time = measureTime {
                joinAll(
                    launch {
                        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Pair<Path, Path>, String>>()
                        pathsForType.asSequence().asFlow().concurrent()
                            .flatMap { (type, paths) -> paths.asFlow().map { type to it } }
                            .collect { (type, packTypeRoot) ->
                                val strategy = CachingStrategy.PackTypeRoot(packTypeRoot, type.directory)
                                consumePackType(packTypeRoot, strategy, directoryToFiles)
                            }
                        for ((path, files) in directoryToFiles) {
                            this@VanillaPackResourcesCache.directoryToFiles[path]!!.complete(files)
                        }
                    },
                    launch { roots.asFlow().concurrent().collect { consumeRoot(it) } }
                )
                allCompleted.complete()
            }
            Lazyyyyy.logger.debug("Cache vanilla pack ${pack.packId()} in $time")
        }

    override fun getNamespaces(type: PackType?): Set<String> {
        // Vanilla cached the namespace when constructing
        throw UnsupportedOperationException()
    }
}