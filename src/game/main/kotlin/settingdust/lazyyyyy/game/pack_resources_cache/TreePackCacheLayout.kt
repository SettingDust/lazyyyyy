package settingdust.lazyyyyy.game.pack_resources_cache

import kotlinx.coroutines.*
import net.minecraft.server.packs.PackType
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object TreePackCacheLayout : PackCacheLayout {
    override suspend fun cachePack(cache: PackCacheCore): PackCacheLayoutResult = coroutineScope {
        // Phase 1: Quickly collect namespaces
        val namespaces = ConcurrentHashMap.newKeySet<String>()
        for (root in cache.roots) {
            for (path in root.listDirectoryEntries()) {
                if (path.isDirectory()) {
                    namespaces += path.name
                }
            }
        }

        // Phase 2: Perform file indexing asynchronously in the background
        val indexing = launch { indexFiles(cache) }

        PackCacheLayoutResult(
            PackType.entries.associateWith { namespaces as Set<String> }
        ) { indexing.join() }
    }

    private suspend fun indexFiles(cache: PackCacheCore) = coroutineScope {
        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, String>>()
        cache.roots.map { root ->
            async { consumePackType(cache, this, root, directoryToFiles) }
        }.awaitAll()
        for ((path, files) in directoryToFiles) {
            cache.directoryToFiles[path]!!.complete(files)
        }
    }

    private suspend fun consumePackType(
        cache: PackCacheCore,
        scope: CoroutineScope,
        directory: Path,
        directoryToFiles: MutableMap<String, MutableMap<Path, String>>
    ) {
        val strategy = PackCachePathStrategy.PackRoot(directory, directory)
        directory.listDirectoryEntries().map { path ->
            scope.async {
                if (path.isDirectory()) {
                    cache.indexResourceDirectory(path, directoryToFiles, strategy)
                } else {
                    cache.indexFile(scope, path, strategy)
                }
            }
        }.awaitAll()
    }
}
