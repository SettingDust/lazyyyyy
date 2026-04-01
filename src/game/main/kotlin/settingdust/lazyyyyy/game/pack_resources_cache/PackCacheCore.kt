package settingdust.lazyyyyy.game.pack_resources_cache

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import org.apache.logging.log4j.LogManager
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.iterator
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

abstract class PackCacheCore(val pack: PackResources, roots: List<Path>) : Closeable {
    companion object {
        val JOINER: Joiner = Joiner.on('/').useForNull("null")

        val logger = LogManager.getLogger()

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }

        val coroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                logger.error("Error loading pack cache in $context", throwable)
        }
    }

    val roots: List<Path> = roots.map {
        if (it.isDirectory()) {
            it
        } else {
            FileSystems.newFileSystem(it).getPath("") ?: error("Path $it is not valid")
        }
    }

    val scope =
        CoroutineScope(Dispatchers.IO + CoroutineName("Pack cache") + coroutineExceptionHandler)

    val allCompleted = Job()

    val files: MutableMap<String, CompletableDeferred<Path>> = ConcurrentHashMap()
    val directoryToFiles: MutableMap<String, CompletableDeferred<Map<Path, String>>> = ConcurrentHashMap()

    val pathToRoot: MutableMap<Path, Path> = ConcurrentHashMap()

    fun join(vararg paths: String?) = when (paths.size) {
        0 -> ""
        1 -> paths[0] ?: "null"
        else -> JOINER.join(paths.iterator())
    } ?: error("Paths shouldn't be null '${paths.joinToString()}'")

    abstract fun getNamespaces(type: PackType?): Set<String>

    fun getResource(type: PackType?, location: ResourceLocation) =
        getResource("${type?.directory?.let { "$it/" } ?: ""}${location.namespace}/${location.path}")

    fun getResource(pathString: String): IoSupplier<InputStream>? {
        val path = getOrWaitResource(pathString) ?: return null
        return try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    fun listResources(type: PackType?, namespace: String, prefix: String, output: PackResources.ResourceOutput) {
        val filesInDir = listOrWaitResources(type, namespace, prefix) ?: return

        runBlocking(scope.coroutineContext) {
            for ((path, relativeString) in filesInDir) {
                val location = ResourceLocation.tryBuild(namespace, relativeString)
                if (location == null) {
                    logger.warn("Invalid path $namespace:$path in pack, ignoring")
                    continue
                }
                output.accept(location, IoSupplier.create(path))
            }
        }
    }

    override fun close() {
        val job = scope.coroutineContext[Job] ?: return
        if (!job.isCompleted) job.cancel(CancellationException("Pack closed"))
        runBlocking { job.join() }
    }

    private fun getOrWaitResource(path: String): Path? {
        return getOrWait { files[path] }
    }

    private fun listOrWaitResources(
        type: PackType?,
        namespace: String,
        prefix: String
    ): Map<Path, String>? {
        val pathString = "${type?.directory?.let { "$it/" } ?: ""}$namespace/$prefix"
        return getOrWait { directoryToFiles[pathString] }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> getOrWait(getter: () -> CompletableDeferred<T>?): T? {
        val deferred = getter()
        if (deferred != null) {
            return if (deferred.isCompleted) deferred.getCompleted()
            else runBlocking(scope.coroutineContext) { deferred.await() }
        }
        if (allCompleted.isCompleted) {
            return getter()?.getCompleted()
        }
        runBlocking(scope.coroutineContext) { allCompleted.join() }
        return getter()?.getCompleted()
    }
}

suspend fun PackCacheCore.indexFile(
    scope: CoroutineScope,
    file: Path,
    strategy: PackCachePathStrategy
) {
    indexFileTask(scope, file, strategy).join()
}

suspend fun PackCacheCore.indexResourceDirectory(
    directory: Path,
    directoryToFiles: MutableMap<String, MutableMap<Path, String>>,
    strategy: PackCachePathStrategy
) {
    val relativePath = strategy.getRootRelativePath(directory)
    val relativePathString = strategy.getRelativePathString(relativePath)
    val namespaceRoot = strategy.getNamespaceRoot(directory)?.run {
        if (directory.isAbsolute) toAbsolutePath() else this
    }
    val shouldCacheDirectoryToFiles = namespaceRoot != null && (directory.nameCount - namespaceRoot.nameCount) >= 1
    if (shouldCacheDirectoryToFiles) {
        directoryToFiles.computeIfAbsent(relativePathString) { ConcurrentHashMap() }
        this.directoryToFiles.computeIfAbsent(relativePathString) {
            CompletableDeferred()
        }
    }

    coroutineScope {
        val tasks = mutableListOf<Job>()
        val entries = directory.listDirectoryEntries()
        for (path in entries) {
            if (path.isDirectory()) {
                tasks += launch { indexResourceDirectory(path, directoryToFiles, strategy) }
            } else {
                val fileTask = indexFileTask(this, path, strategy)
                tasks += fileTask
                if (shouldCacheDirectoryToFiles) {
                    tasks += after(fileTask) {
                        directoryToFiles[relativePathString]!![path] =
                            PackCacheCore.JOINER.join(namespaceRoot.relativize(path))
                    }
                }
            }
        }
        tasks.joinAll()
    }
}

suspend fun PackCacheCore.indexRootDirectory(
    scope: CoroutineScope,
    directory: Path,
    strategy: PackCachePathStrategy
) {
    val entries = directory.listDirectoryEntries()
    for (path in entries) {
        if (path.isDirectory()) {
            indexRootDirectory(scope, path, strategy)
        } else {
            indexFile(scope, path, strategy)
        }
    }
}

private fun PackCacheCore.indexFileTask(
    scope: CoroutineScope,
    file: Path,
    strategy: PackCachePathStrategy
): Job = scope.launch {
    val relativePath = strategy.getRootRelativePath(file)
    val key = strategy.getRelativePathString(relativePath)
    pathToRoot[file] = strategy.root
    files.putIfAbsent(key, CompletableDeferred(file))
}

private fun CoroutineScope.after(
    dependency: Job,
    block: suspend () -> Unit
): Job = launch {
    dependency.join()
    block()
}
