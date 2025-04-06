package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CancellationException
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
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Duration.Companion.nanoseconds

typealias FileConsumer = (Path) -> Unit?

abstract class PackResourcesCache(val pack: PackResources, val roots: List<Path>) : Closeable {
    companion object {
        val JOINER: Joiner = Joiner.on('/').useForNull("null")

        val logger = LogManager.getLogger()

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
        val path = getOrWaitResource(path) ?: return null
        return try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    @OptIn(ExperimentalPathApi::class, ExperimentalCoroutinesApi::class)
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
        try {
            scope.cancel()
        } catch (_: CancellationException) {
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getOrWaitResource(path: String): Path? {
        return getOrWait { files[path] }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listOrWaitResources(
        type: PackType?,
        namespace: String,
        prefix: String
    ): Map<Path, String>? {
        val pathString = "${type?.directory?.let { "${it}/" } ?: ""}$namespace/$prefix"
        return getOrWait {
            directoryToFiles[pathString]
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> getOrWait(getter: () -> CompletableDeferred<T>?): T? {
        val deferred = getter()
        if (deferred != null) {
            return if (deferred.isCompleted) deferred.getCompleted()
            else runBlocking { deferred.await() }
        } else if (allCompleted.isCompleted) {
            // ConcurrentHashMap may overlap get and put.
            return getter()?.getCompleted()
        } else {
            var result = getter()
            if (result != null) {
                return if (result.isCompleted) result.getCompleted()
                else runBlocking { result.await() }
            }
            runBlocking {
                while (result == null) {
                    delay(300.nanoseconds)
                    result = getter()
                    if (allCompleted.isCompleted) break
                }
            }
            return if (result == null) null
            else runBlocking { result.await() }
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