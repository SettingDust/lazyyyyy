package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
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
import settingdust.lazyyyyy.util.race
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Duration.Companion.nanoseconds

abstract class PackResourcesCache(val pack: PackResources, val roots: List<Path>) : Closeable {
    companion object {
        val JOINER: Joiner = Joiner.on('/').useForNull("null")

        val logger = LogManager.getLogger()

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }
    }

    val scope =
        CoroutineScope(Dispatchers.IO + CoroutineName("Pack cache #${pack.packId()}") + CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                Lazyyyyy.logger.error("Error loading pack cache in $context", throwable)
        })

    val allCompleted = Job()

    val files: MutableMap<String, CompletableDeferred<Path>> = ConcurrentHashMap()
    val directoryToFiles: MutableMap<String, CompletableDeferred<Map<Path, String>>> = ConcurrentHashMap()

    fun join(vararg paths: String?) = when (paths.size) {
        0 -> ""
        1 -> paths[0] ?: "null"
        else -> JOINER.join(paths.iterator())
    } ?: error("Paths shouldn't be null '${paths.joinToString()}'")

    @OptIn(ExperimentalCoroutinesApi::class)
    abstract fun getNamespaces(type: PackType?): Set<String>

    fun getResource(type: PackType?, location: ResourceLocation) =
        getResource("${type?.directory?.let { "${it}/" } ?: ""}${location.namespace}/${location.path}")

    fun getResource(path: String): IoSupplier<InputStream>? {
        val path = getOrCacheResource(path) ?: return null
        return try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    @OptIn(ExperimentalPathApi::class, ExperimentalCoroutinesApi::class)
    fun listResources(type: PackType?, namespace: String, prefix: String, output: PackResources.ResourceOutput) {
        val filesInDir = listOrCacheResources(type, namespace, prefix) ?: return
        runBlocking(Dispatchers.IO) {
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
        scope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getOrCacheResource(path: String) = runBlocking {
        val deferred = files[path]
        if (deferred?.isCompleted == true) deferred.getCompleted()
        else (deferred ?: race(
            {
                while (path !in files) {
                    delay(50.nanoseconds)
                }
                files[path]
            },
            {
                allCompleted.join()
                files[path]
            }))?.await()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listOrCacheResources(
        type: PackType?,
        namespace: String,
        prefix: String
    ) = runBlocking {
        val pathString = "${type?.directory?.let { "${it}/" } ?: ""}$namespace/$prefix"
        val deferred = directoryToFiles[pathString]
        if (deferred?.isCompleted == true) deferred.getCompleted()
        else
            (deferred ?: race(
                {
                    while (pathString !in directoryToFiles) {
                        delay(50.nanoseconds)
                    }
                    directoryToFiles[pathString]
                },
                {
                    allCompleted.join()
                    null
                }
            ))?.await()
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
            files.putIfAbsent(key, CompletableDeferred(file))
        },
        scope.launch { fileConsumer?.invoke(file) }
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun PackResourcesCache.consumeResourceDirectory(
    directory: Path,
    directoryToFiles: MutableMap<String, MutableMap<Path, Deferred<String>>>,
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
        val fileConsumer = if (shouldCacheDirectoryToFiles)
            { path: Path ->
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory/$relativePathString#fileConsumer/$path] caching") }
                directoryToFiles[relativePathString]!![path] = async { JOINER.join(namespaceRoot.relativize(path)) }
                fileConsumer?.invoke(path)
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory/$relativePathString#fileConsumer/$path] cached") }
            } else fileConsumer

        val entries = directory.listDirectoryEntries()
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory#entries/$relativePathString] caching $entries") }
        entries.asFlow().concurrent().collect { path ->
            if (path.isDirectory()) {
                consumeResourceDirectory(path, directoryToFiles, strategy, fileConsumer)
            } else {
                consumeFile(this, path, strategy, fileConsumer)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#directory#entries/$relativePathString] cached") }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
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