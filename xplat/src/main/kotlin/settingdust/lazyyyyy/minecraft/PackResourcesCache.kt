package settingdust.lazyyyyy.minecraft

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import settingdust.lazyyyyy.Lazyyyyy
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
    val namespacePaths: MutableMap<String, MutableSet<Path>> = Object2ReferenceOpenHashMap()
    val directoryToFiles: MutableMap<String, MutableSet<Path>> = Object2ReferenceOpenHashMap()
    val namespaces: MutableMap<PackType, MutableSet<String>> = Object2ReferenceOpenHashMap()

    protected val scope = CoroutineScope(Dispatchers.IO)

    init {
        Lazyyyyy.logger.debug("Loading pack {} {}", pack.packId(), pack, Throwable())
    }

    fun join(vararg paths: String) = when (paths.size) {
        0 -> ""
        1 -> paths[0]
        else -> JOINER.join(paths)
    }!!

    fun getNamespaces(type: PackType): Set<String> {
        if (!loadingJob.isCompleted) runBlocking { loadingJob.join() }
        return namespaces[type] ?: emptySet()
    }

    fun getResource(
        type: PackType,
        location: ResourceLocation,
    ) = getResource("${type.directory}/${location.namespace}/${location.path}")

    fun getResource(
        path: String
    ): IoSupplier<InputStream>? {
        if (!loadingJob.isCompleted) runBlocking { loadingJob.join() }
        val path = files[path] ?: return null
        return try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun listResources(type: PackType, namespace: String, prefix: String, output: PackResources.ResourceOutput) {
        if (!loadingJob.isCompleted) runBlocking { loadingJob.join() }
        val namespacePath = namespacePaths["${type.directory}/$namespace"] ?: return
        val filesInDir = directoryToFiles["${type.directory}/$namespace/$prefix"] ?: return
        for (path in filesInDir) {
            val relativePath = namespacePath.firstNotNullOfOrNull {
                try {
                    it.relativize(path)
                } catch (_: IllegalArgumentException) {
                    null
                }
            } ?: error("Can't find namespace path for $path in pack ${pack.packId()}")
            val relativeString = JOINER.join(relativePath)
            val location = ResourceLocation.tryBuild(namespace, relativeString)
            if (location != null) {
                output.accept(location, IoSupplier.create(path))
            } else {
                Lazyyyyy.logger.warn("Invalid path $namespace:$path in pack ${pack.packId()}, ignoring")
            }
        }
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
    fun loadCache() = scope.launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            val files: MutableMap<String, Path> = ConcurrentHashMap()
            val namespacePaths: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()
            val directoryToFiles: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()
            for ((type, roots) in pathsForType) {
                for (root in roots) {
                    val isRoot = root in super.roots
                    root.visitFileTree(fileVisitor {
                        onPreVisitDirectory { directory, attributes ->
                            jobs.add(launch {
                                if (isRoot) {
                                    if (directory.nameCount != 1) return@launch
                                    namespacePaths.getOrPut("${type.directory}/${directory.name}") { ConcurrentHashMap.newKeySet() }
                                        .add(directory)
                                } else {
                                    if (directory.nameCount != 2) return@launch
                                    val relativePath = root.parent.relativize(directory)
                                    val path = JOINER.join(relativePath)
                                    namespacePaths.getOrPut(path) { ConcurrentHashMap.newKeySet() }.add(directory)
                                }
                            })
                            FileVisitResult.CONTINUE
                        }

                        onVisitFile { file, attributes ->
                            jobs.add(launch {
                                val relativePath = root.relativize(file)
                                val pathString = JOINER.join(relativePath)
                                val fileKey = "${type.directory}/$pathString"
                                if (fileKey in files) return@launch
                                files[fileKey] = file
                                if (relativePath.nameCount >= 3) {
                                    var pathString = StringBuilder(type.directory).append('/').append(relativePath.getName(0).name)
                                    for (i in 1 until relativePath.nameCount - 1) {
                                        pathString.append('/').append(relativePath.getName(i).name)
                                        directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }
                                            .add(file)
                                    }
                                }
                            })
                            FileVisitResult.CONTINUE
                        }
                    })
                }
            }

            for (root in roots) {
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
            this@VanillaPackResourcesCache.namespacePaths.putAll(namespacePaths)
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
    fun loadCache() = scope.launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            val files: MutableMap<String, Path> = ConcurrentHashMap()
            val namespacePaths: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()
            val directoryToFiles: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()
            val namespaces: MutableMap<PackType, MutableSet<String>> = ConcurrentHashMap()
            for (root in roots) {
                root.visitFileTree(fileVisitor {
                    onPreVisitDirectory { directory, attributes ->
                        val relativePath = root.relativize(directory)
                        val rootPath = relativePath.firstOrNull() ?: return@onPreVisitDirectory FileVisitResult.CONTINUE
                        if (rootPath.name.isEmpty()) return@onPreVisitDirectory FileVisitResult.CONTINUE
                        val type = packTypeByDirectory[rootPath.name]
                        if (type == null) return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
                        jobs.add(launch {
                            if (relativePath.nameCount != 2) return@launch
                            val path = JOINER.join(relativePath)
                            namespacePaths.getOrPut(path) { ConcurrentHashMap.newKeySet() }.add(directory)
                            namespaces
                                .getOrPut(type) { ConcurrentHashMap.newKeySet() }
                                .add(relativePath.name)
                        })
                        FileVisitResult.CONTINUE
                    }

                    onVisitFile { file, attributes ->
                        jobs.add(launch {
                            val relativePath = root.relativize(file)
                            val pathString = JOINER.join(relativePath)
                            files[pathString] = file
                            if (relativePath.nameCount >= 4) {
                                var pathString = StringBuilder(relativePath.getName(0).name).append('/').append(relativePath.getName(1).name)
                                for (i in 2 until relativePath.nameCount - 1) {
                                    val path = relativePath.getName(i)
                                    pathString.append('/').append(path.name)
                                    directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }
                                        .add(file)
                                }
                            }
                        })
                        FileVisitResult.CONTINUE
                    }
                })
            }
            jobs.joinAll()

            this@SimplePackResourcesCache.files.putAll(files)
            this@SimplePackResourcesCache.namespacePaths.putAll(namespacePaths)
            this@SimplePackResourcesCache.directoryToFiles.putAll(directoryToFiles)
            this@SimplePackResourcesCache.namespaces.putAll(namespaces)
        }
        if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache pack ${pack.packId()} in $time")
        else Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
    }
}