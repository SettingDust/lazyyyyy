package settingdust.lazyyyyy.minecraft

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
    val `lazyyyyy$cache`: PackResourcesCache
}

abstract class PackResourcesCache(val pack: PackResources, val roots: List<Path>) {
    companion object {
        val JOINER = Joiner.on('/')

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }
    }

    protected abstract val loadingJob: Job

    val files: MutableMap<String, Path> = Object2ReferenceOpenHashMap()
    val directoryToFiles: MutableMap<String, MutableSet<Pair<Path, String>>> = Object2ReferenceOpenHashMap()
    val namespaces: MutableMap<PackType, MutableSet<String>> = Object2ReferenceOpenHashMap()

    init {
        Lazyyyyy.logger.debug("Loading pack {} {}", pack.packId(), pack, Throwable())
    }

    fun join(vararg paths: String) = when (paths.size) {
        0 -> ""
        1 -> paths[0]
        else -> JOINER.join(paths)
    }!!

    fun getNamespaces(type: PackType): Set<String> {
        waitForLoading()
        return namespaces[type] ?: emptySet()
    }

    fun getResource(
        type: PackType,
        location: ResourceLocation,
    ) = getResource("${type.directory}/${location.namespace}/${location.path}")

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
    fun listResources(type: PackType, namespace: String, prefix: String, output: PackResources.ResourceOutput) {
        waitForLoading()
        val filesInDir = directoryToFiles["${type.directory}/$namespace/$prefix"] ?: return
        runBlocking(Dispatchers.Default) {
            filesInDir.asFlow().concurrent()
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
    pathsForType: Map<PackType, List<Path>>
) : PackResourcesCache(pack, roots) {
    override val loadingJob: Job
    private val pathsForType: Map<PackType, List<Path>>

    init {
        loadingJob = loadCache()
        this.pathsForType = pathsForType
    }

    @OptIn(ExperimentalPathApi::class)
    fun loadCache() = CoroutineScope(SupervisorJob() + CoroutineName("Vanilla Pack Cache")).launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            val files: MutableMap<String, Path> = ConcurrentHashMap()
            val directoryToFiles: MutableMap<String, MutableSet<Pair<Path, String>>> = ConcurrentHashMap()
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
                                    directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }
                                        .add(file to JOINER.join(namespaceRoot.relativize(file)))
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
            this@VanillaPackResourcesCache.files.putAll(files)
            this@VanillaPackResourcesCache.directoryToFiles.putAll(directoryToFiles)
        }
        if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache vanilla pack ${pack.packId()} in $time")
        else Lazyyyyy.logger.debug("Cache vanilla pack ${pack.packId()} in $time")
    }
}

open class SimplePackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    override val loadingJob = loadCache()

    @OptIn(ExperimentalPathApi::class)
    fun loadCache() = CoroutineScope(SupervisorJob() + CoroutineName("Simple Pack Cache #${pack.packId()}")).launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            val files: MutableMap<String, Path> = ConcurrentHashMap()
            val directoryToFiles: MutableMap<String, MutableSet<Pair<Path, String>>> = ConcurrentHashMap()
            val namespaces: MutableMap<PackType, MutableSet<String>> = ConcurrentHashMap()
            roots.asFlow().concurrent().collect { root ->
                root.visitFileTree(fileVisitor {
                    onPreVisitDirectory { directory, attributes ->
                        val relativePath = root.relativize(directory)
                        val rootPath = relativePath.firstOrNull() ?: return@onPreVisitDirectory FileVisitResult.CONTINUE
                        if (rootPath.name.isEmpty()) return@onPreVisitDirectory FileVisitResult.CONTINUE
                        val type = packTypeByDirectory[rootPath.name]
                        if (type == null) return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
                        jobs.add(launch {
                            if (relativePath.nameCount != 2) return@launch
                            namespaces
                                .getOrPut(type) { ConcurrentHashMap.newKeySet() }
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
                            for (i in 2 until relativePath.nameCount - 1) {
                                val directory = file.subpath(0, i + root.nameCount)
                                directoryToFiles.getOrPut(directory.toString()) { ConcurrentHashMap.newKeySet() }
                                    .add(file to JOINER.join(namespaceRoot.relativize(file)))
                            }
                        })
                        FileVisitResult.CONTINUE
                    }
                })
            }
            jobs.joinAll()

            this@SimplePackResourcesCache.files.putAll(files)
            this@SimplePackResourcesCache.directoryToFiles.putAll(directoryToFiles)
            this@SimplePackResourcesCache.namespaces.putAll(namespaces)
        }
        if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache pack ${pack.packId()} in $time")
        else Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
    }
}