package settingdust.lazyyyyy.minecraft.pack_resources_cache

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import org.apache.logging.log4j.LogManager
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCache.Companion.JOINER
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import settingdust.lazyyyyy.util.mapNotNull
import settingdust.lazyyyyy.util.merge
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Duration.Companion.nanoseconds

typealias FileConsumer = (Path) -> Unit?

abstract class PackResourcesCache(val pack: PackResources, val roots: List<Path>) : Closeable {
    companion object {
        val JOINER: Joiner = Joiner.on('/').useForNull("null")

        val logger = LogManager.getLogger()!!

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }

        val coroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                Lazyyyyy.logger.error("Error loading pack cache in $context", throwable)
        }
    }

    val scope =
        CoroutineScope(Dispatchers.IO + CoroutineName("Pack cache #${pack.packId()}") + coroutineExceptionHandler)

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
        getResource("${type?.directory?.let { "${it}/" } ?: ""}${location.namespace}/${location.path}")

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
            filesInDir.asSequence().asFlow().concurrent()
                .mapNotNull { (path, relativeString) ->
                    val location = ResourceLocation.tryBuild(namespace, relativeString)
                    if (location == null) Lazyyyyy.logger.warn("Invalid path $namespace:$path in pack ${pack.packId()}, ignoring")
                    return@mapNotNull location?.let { it to path }
                }
                .merge(false)
                .collect { (location, path) ->
                    output.accept(location, IoSupplier.create(path))
                }
        }
    }

    override fun close() {
        val job = scope.coroutineContext[Job] ?: return
        if (job.isCancelled) job.cancel(CancellationException("Pack ${pack.packId()} closed"))
        if (job.isCompleted) runBlocking { job.join() }
    }

    private fun getOrWaitResource(path: String): Path? {
        return getOrWait { files[path] }
    }

    private fun listOrWaitResources(
        type: PackType?,
        namespace: String,
        prefix: String
    ): Map<Path, String>? {
        val pathString = "${type?.directory?.let { "${it}/" } ?: ""}$namespace/$prefix"
        return getOrWait { directoryToFiles[pathString] }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> getOrWait(getter: () -> CompletableDeferred<T>?): T? {
        val deferred = getter()
        if (deferred != null) {
            return if (deferred.isCompleted) deferred.getCompleted()
            else runBlocking(scope.coroutineContext) { deferred.await() }
        } else if (allCompleted.isCompleted) {
            // ConcurrentHashMap may overlap get and put.
            return getter()?.getCompleted()
        } else {
            var result = getter()
            if (result != null) {
                return if (result.isCompleted) result.getCompleted()
                else runBlocking(scope.coroutineContext) { result!!.await() }
            }
            runBlocking(scope.coroutineContext) {
                while (result == null) {
                    delay(300.nanoseconds)
                    result = getter()
                    if (allCompleted.isCompleted) break
                }
            }
            return if (result == null) null
            else runBlocking(scope.coroutineContext) { result!!.await() }
        }
    }
}

suspend fun PackResourcesCache.consumeFile(
    scope: CoroutineScope,
    file: Path,
    strategy: CachingStrategy,
    fileConsumer: FileConsumer? = null
) {
    val relativePath = strategy.getRootRelativePath(file)
    joinAll(
        scope.launch {
            val key = strategy.getRelativePathString(relativePath)
            pathToRoot[file] = strategy.root
            files.putIfAbsent(key, CompletableDeferred(file))
        },
        scope.launch { fileConsumer?.invoke(file) }
    )
}

suspend fun PackResourcesCache.consumeResourceDirectory(
    directory: Path,
    directoryToFiles: MutableMap<String, MutableMap<Path, String>>,
    strategy: CachingStrategy,
    fileConsumer: FileConsumer? = null
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
        val newFileConsumer = if (shouldCacheDirectoryToFiles)
            { path: Path ->
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory/$relativePathString#fileConsumer/$path] caching") }
                pathToRoot[path] = strategy.root
                directoryToFiles[relativePathString]!![path] = JOINER.join(namespaceRoot!!.relativize(path))
                fileConsumer?.invoke(path)
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory/$relativePathString#fileConsumer/$path] cached") }
            } else fileConsumer

        val entries = directory.listDirectoryEntries()
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory#entries/$relativePathString] caching $entries") }
        entries.asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                consumeResourceDirectory(path, directoryToFiles, strategy, newFileConsumer)
            } else {
                consumeFile(this, path, strategy, newFileConsumer)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory#entries/$relativePathString] cached") }
    }
}

suspend fun PackResourcesCache.consumeRootDirectory(
    scope: CoroutineScope,
    directory: Path,
    strategy: CachingStrategy
) {
    directory.listDirectoryEntries().asFlow().concurrent().collect { path ->
        if (path.isDirectory()) {
            consumeRootDirectory(scope, path, strategy)
        } else {
            consumeFile(scope, path, strategy)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun PackResourcesCache.filesToCache(
    roots: MutableMap<String, PackResourcesCacheDataEntry>,
    rootsHashes: Map<Path, String>
) = files.asSequence().asFlow().concurrent().collect { (key, fileDeferred) ->
    val file = fileDeferred.getCompleted()
    val root = pathToRoot[file] ?: error("Missing root for $file")
    val rootHash = rootsHashes[root] ?: error("Missing root hash for $root to $file. Roots: $rootsHashes")
    val rootEntry = (roots[rootHash] ?: error("Missing root entry for $rootHash"))
    rootEntry.files[key] = root.relativize(file).toString()
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun PackResourcesCache.directoryToFilesToCache(
    roots: MutableMap<String, PackResourcesCacheDataEntry>,
    rootsHashes: Map<Path, String>
) = directoryToFiles.asSequence().asFlow().concurrent().collect { (key, value) ->
    val map = value.getCompleted()
    map.asSequence().asFlow().concurrent().collect {
        val (path, string) = it
        val root = pathToRoot[path] ?: error("Missing root for $path")
        val rootHash = rootsHashes[root] ?: error("Missing root hash for $root to $path. Roots: $rootsHashes")
        val rootEntry = (roots[rootHash] ?: error("Missing root entry for $rootHash"))
        rootEntry.directoryToFiles
            .computeIfAbsent(key) { ConcurrentHashMap() }[root.relativize(path).toString()] = string
    }
}