package settingdust.lazyyyyy.game.pack_resources_cache

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.server.packs.PackType
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

internal object DefaultPackCacheLayout : PackCacheLayout {
    override suspend fun cachePack(cache: PackCacheCore): PackCacheLayoutResult = coroutineScope {
        // Phase 1: Quickly collect all namespaces
        val namespaceResults = cache.roots.map { root ->
            async { collectNamespaces(root) }
        }.awaitAll()
        val namespaces = mergeNamespaces(namespaceResults)

        // Phase 2: Perform file indexing asynchronously in the background
        val indexing = launch { indexFiles(cache) }

        PackCacheLayoutResult(namespaces) { indexing.join() }
    }

    /**
     * Quickly collect namespaces by scanning directory structure only, without file indexing
     */
    private fun collectNamespaces(root: Path): Map<PackType, Set<String>> {
        val namespaces = ConcurrentHashMap<PackType, MutableSet<String>>()
        for (path in root.listDirectoryEntries()) {
            if (path.isDirectory()) {
                val relativePath = root.relativize(path)
                val firstPath = relativePath.firstOrNull()
                val packType = PackCacheCore.packTypeByDirectory[firstPath?.name] ?: continue
                val typeNamespaces = collectPackTypeNamespaces(path)
                namespaces.computeIfAbsent(packType) { ConcurrentHashMap.newKeySet() }.addAll(typeNamespaces)
            }
        }
        return namespaces.mapValues { it.value }
    }

    private fun collectPackTypeNamespaces(directory: Path): Set<String> =
        directory.listDirectoryEntries()
            .filter { it.isDirectory() }
            .map { it.name }
            .toSet()

    /**
     * Perform complete file indexing (in the background)
     */
    private suspend fun indexFiles(cache: PackCacheCore) = coroutineScope {
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
        cache.roots.map { root ->
            async { indexRoot(cache, root, directoryToFiles) }
        }.awaitAll()
        for ((path, files) in directoryToFiles) {
            cache.directoryToFiles[path]!!.complete(files)
        }
    }

    private suspend fun indexRoot(
        cache: PackCacheCore,
        root: Path,
        directoryToFiles: ConcurrentHashMap<String, MutableMap<Path, String>>
    ) = coroutineScope {
        val rootStrategy = PackCachePathStrategy.PackRoot(root, null)
        root.listDirectoryEntries().map { path ->
            async {
                if (path.isDirectory()) {
                    val relativePath = root.relativize(path)
                    val firstPath = relativePath.firstOrNull()
                    val packType = PackCacheCore.packTypeByDirectory[firstPath?.name] ?: return@async
                    indexPackType(cache, path, PackCachePathStrategy.PackRoot(root, path), directoryToFiles)
                } else {
                    cache.indexFile(this, path, rootStrategy)
                }
            }
        }.awaitAll()
    }

    private suspend fun indexPackType(
        cache: PackCacheCore,
        directory: Path,
        strategy: PackCachePathStrategy,
        directoryToFiles: ConcurrentHashMap<String, MutableMap<Path, String>>
    ) = coroutineScope {
        directory.listDirectoryEntries().map { path ->
            async {
                if (path.isDirectory()) {
                    cache.indexResourceDirectory(path, directoryToFiles, strategy)
                } else {
                    cache.indexFile(this, path, strategy)
                }
            }
        }.awaitAll()
    }

    private fun mergeNamespaces(
        results: List<Map<PackType, Set<String>>>
    ): Map<PackType, Set<String>> {
        val merged = ConcurrentHashMap<PackType, MutableSet<String>>()
        for (result in results) {
            for ((type, set) in result) {
                merged.computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }.addAll(set)
            }
        }
        return merged.mapValues { it.value }
    }
}
