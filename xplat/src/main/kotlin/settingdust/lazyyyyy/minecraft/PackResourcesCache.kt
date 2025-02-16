package settingdust.lazyyyyy.minecraft

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.collect
import settingdust.lazyyyyy.concurrent
import settingdust.lazyyyyy.flatMap
import settingdust.lazyyyyy.mapNotNull
import settingdust.lazyyyyy.merge
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.fileVisitor
import kotlin.io.path.name
import kotlin.io.path.visitFileTree
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

interface CachingPackResources {
    val `lazyyyyy$cache`: PackResourcesCache?
}

val supported = setOf(
    "com.ferreusveritas.dynamictrees.resources.ModTreeResourcePack",
    "com.ferreusveritas.dynamictrees.resources.FlatTreeResourcePack"
)

abstract class PackResourcesCache(val pack: PackResources, val roots: List<Path>) {
    companion object {
        val JOINER = Joiner.on('/').useForNull("null")

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }
    }

    protected abstract var loadingJob: Job

    var files: MutableMap<String, Path> = ConcurrentHashMap()
        protected set
    var directoryToFiles: MutableMap<String, MutableMap<Path, String>> = ConcurrentHashMap()
        protected set
    var namespaces: MutableMap<PackType, MutableSet<String>> = ConcurrentHashMap()
        protected set

    fun join(vararg paths: String?) = when (paths.size) {
        0 -> ""
        1 -> paths[0] ?: "null"
        else -> JOINER.join(paths.iterator())
    } ?: error("Paths shouldn't be null '${paths.joinToString()}'")

    open fun getNamespaces(type: PackType?): Set<String> {
        waitForLoading()
        return namespaces[type] ?: emptySet()
    }

    fun getResource(
        type: PackType?,
        location: ResourceLocation,
    ) = getResource("${type?.directory?.let { "${it}/" } ?: ""}${location.namespace}/${location.path}")

    fun getResource(
        path: String
    ): IoSupplier<InputStream>? {
        waitForLoading()
        val path = files[path] ?: return null
        return try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun listResources(type: PackType?, namespace: String, prefix: String, output: PackResources.ResourceOutput) {
        waitForLoading()
        val filesInDir =
            directoryToFiles["${type?.directory?.let { "${it}/" } ?: ""}$namespace/$prefix"] ?: return
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

    private fun waitForLoading() {
        if (!loadingJob.isCompleted) runBlocking { loadingJob.join() }
    }
}

class VanillaPackResourcesCache(
    pack: PackResources,
    roots: List<Path>,
    private val pathsForType: Map<PackType, List<Path>>
) : PackResourcesCache(pack, roots) {
    override var loadingJob: Job

    init {
        loadingJob = loadCache()
    }

    @OptIn(ExperimentalPathApi::class)
    fun loadCache() =
        CoroutineScope(Dispatchers.IO + CoroutineName("Vanilla Pack Cache") + CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                Lazyyyyy.logger.error("Error loading vanilla pack cache in $context", throwable)
        }).launch {
            val time = measureTime {
                val jobs = mutableListOf<Job>()
                pathsForType.asSequence().asFlow().concurrent()
                    .flatMap { (type, paths) -> paths.asFlow().map { type to it } }
                    .collect { (type, root) ->
                        root.visitFileTree(fileVisitor {
                            onVisitFile { file, attributes ->
                                val relativePath by lazy { root.relativize(file) }
                                jobs.add(launch {
                                    val pathString = JOINER.join(relativePath)
                                    val fileKey = "${type.directory}/$pathString"
                                    files.putIfAbsent(fileKey, file)
                                })
                                jobs.add(launch {
                                    if (relativePath.nameCount < 3) return@launch
                                    val namespace = relativePath.getName(0).name
                                    var pathString =
                                        StringBuilder(type.directory).append('/').append(namespace)
                                    val namespaceRoot = root.resolve(namespace).let {
                                        if (file.isAbsolute) it.toAbsolutePath() else it
                                    }
                                    for (i in 1 until relativePath.nameCount - 1) {
                                        pathString.append('/').append(relativePath.getName(i).name)
                                        directoryToFiles
                                            .computeIfAbsent(pathString.toString()) { ConcurrentHashMap() }
                                            .put(file, JOINER.join(namespaceRoot.relativize(file)))
                                    }
                                })
                                FileVisitResult.CONTINUE
                            }
                        })
                    }

                roots.asFlow().concurrent().collect { root ->
                    root.visitFileTree(fileVisitor {
                        onPreVisitDirectory { directory, attributes ->
                            if (directory.nameCount == 1 && directory.name in packTypeByDirectory) FileVisitResult.SKIP_SUBTREE
                            else FileVisitResult.CONTINUE
                        }

                        onVisitFile { file, attributes ->
                            jobs.add(launch {
                                val relativePath = root.relativize(file)
                                val pathString = JOINER.join(relativePath)
                                files[pathString] = file
                            })
                            FileVisitResult.CONTINUE
                        }
                    })
                }

                jobs.joinAll()
            }
            if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache vanilla pack ${pack.packId()} in $time")
            else Lazyyyyy.logger.debug("Cache vanilla pack ${pack.packId()} in $time")
        }
}

open class SimplePackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    override var loadingJob = loadCache()

    @OptIn(ExperimentalPathApi::class)
    open fun loadCache() =
        CoroutineScope(Dispatchers.IO + CoroutineName("Simple Pack Cache #${pack.packId()}") + CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                Lazyyyyy.logger.error("Error loading pack cache in $context", throwable)
        }).launch {
            val time = measureTime {
                val jobs = mutableListOf<Job>()
                roots.asFlow().concurrent().collect { root ->
                    root.visitFileTree(fileVisitor {
                        onPreVisitDirectory { directory, attributes ->
                            val relativePath = root.relativize(directory)
                            val rootPath =
                                relativePath.firstOrNull() ?: return@onPreVisitDirectory FileVisitResult.CONTINUE
                            if (rootPath.name.isEmpty()) return@onPreVisitDirectory FileVisitResult.CONTINUE
                            val type = packTypeByDirectory[rootPath.name]
                            if (type == null) return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
                            jobs.add(launch {
                                if (relativePath.nameCount != 2) return@launch
                                namespaces
                                    .computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }
                                    .add(relativePath.name)
                            })
                            FileVisitResult.CONTINUE
                        }

                        onVisitFile { file, attributes ->
                            val relativePath by lazy { root.relativize(file) }
                            jobs.add(launch {
                                val pathString = JOINER.join(relativePath)
                                files[pathString] = file
                            })
                            jobs.add(launch {
                                if (relativePath.nameCount < 4)
                                    return@launch
                                val namespaceRoot = root.resolve(relativePath.subpath(0, 2)).let {
                                    if (file.isAbsolute) it.toAbsolutePath() else it
                                }
                                for (i in 2 until relativePath.nameCount) {
                                    val directory = relativePath.subpath(0, i)
                                    directoryToFiles
                                        .computeIfAbsent(directory.toString()) { ConcurrentHashMap() }
                                        .put(file, JOINER.join(namespaceRoot.relativize(file)))
                                }
                            })
                            FileVisitResult.CONTINUE
                        }
                    })
                }
                jobs.joinAll()
            }
            if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache pack ${pack.packId()} in $time")
            else Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
        }
}