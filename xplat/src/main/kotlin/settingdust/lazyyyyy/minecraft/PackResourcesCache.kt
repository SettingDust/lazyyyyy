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
import kotlin.io.path.relativeTo
import kotlin.io.path.visitFileTree
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

interface CachingPackResources {
    val `lazyyyyy$cache`: PackResourcesCache
}

class VanillaPackResourcesCache(
    val rootPaths: List<Path>,
    val pathsForType: Map<PackType, List<Path>>,
    pack: PackResources
) : PackResourcesCache(rootPaths[0], pack) {
    override val files: MutableMap<String, Path> = ConcurrentHashMap()
    override val namespacePaths: MutableMap<String, Path> = ConcurrentHashMap()
    override val directoryToFiles: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()

    init {
        Lazyyyyy.logger.debug("Loading pack {}", pack, Throwable())
    }

    @OptIn(ExperimentalPathApi::class)
    override fun loadCache() = scope.launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            for ((type, roots) in pathsForType) {
                for (root in roots) {
                    root.visitFileTree(fileVisitor {
                        onPreVisitDirectory { directory, attributes ->
                            jobs.add(launch {
                                if (directory.nameCount != 1) return@launch
                                namespacePaths["${type.directory}/${directory.name}"] = directory
                            })
                            FileVisitResult.CONTINUE
                        }

                        onVisitFile { file, attributes ->
                            jobs.add(launch {
                                val relativePath = root.relativize(file)
                                val pathString = JOINER.join(relativePath)
                                files["${type.directory}/$pathString"] = file
                                directoryToFiles.getOrPut("") { ConcurrentHashMap.newKeySet() }.add(file)
                                if (relativePath.parent != null && relativePath.parent.nameCount != 0) {
                                    var pathString =
                                        StringBuilder("${type.directory}/${relativePath.parent.first().name}")
                                    directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }
                                        .add(file)
                                    for (i in 1 until relativePath.parent.nameCount) {
                                        val path = relativePath.parent.getName(i)
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
            }
            for (root in rootPaths) {
                root.visitFileTree(fileVisitor {
                    onPreVisitDirectory { directory, attributes ->
                        if (directory.nameCount == 1 && directory.name in PackResourcesCache.packTypeByDirectory) FileVisitResult.SKIP_SUBTREE
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

open class PackResourcesCache(val root: Path, val pack: PackResources) {
    companion object {
        val JOINER = Joiner.on('/')

        val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }
    }

    open val files: MutableMap<String, Path> = ConcurrentHashMap()
    open val namespacePaths: MutableMap<String, Path> = ConcurrentHashMap()
    open val directoryToFiles: MutableMap<String, MutableSet<Path>> = ConcurrentHashMap()
    val namespaces: MutableMap<PackType, MutableSet<String>> = ConcurrentHashMap()

    val scope = CoroutineScope(Dispatchers.IO)
    val loadingJob = loadCache()

    init {
        Lazyyyyy.logger.debug("Loading pack {}", pack, Throwable())
    }

    @OptIn(ExperimentalPathApi::class)
    open fun loadCache() = scope.launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
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
                        namespacePaths[path] = directory
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
                        directoryToFiles.getOrPut("") { ConcurrentHashMap.newKeySet() }.add(file)
                        if (relativePath.parent != null && relativePath.parent.nameCount != 0) {
                            var pathString = StringBuilder(relativePath.parent.first().name)
                            directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }.add(file)
                            for (i in 1 until relativePath.parent.nameCount) {
                                val path = relativePath.parent.getName(i)
                                pathString.append('/').append(path.name)
                                directoryToFiles.getOrPut(pathString.toString()) { ConcurrentHashMap.newKeySet() }
                                    .add(file)
                            }
                        }
                    })
                    FileVisitResult.CONTINUE
                }
            })
            jobs.joinAll()
        }
        if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache pack ${pack.packId()} in $time")
        else Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
    }

    fun join(vararg paths: String) = when (paths.size) {
        0 -> ""
        1 -> paths[0]
        else -> JOINER.join(paths)
    }

    fun getPath(vararg paths: String) = when (paths.size) {
        0 -> root.fileSystem.getPath("")
        1 -> root.fileSystem.getPath(paths[0])
        else -> root.fileSystem.getPath(paths[0], *paths.drop(1).toTypedArray())
    }

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
            val relativePath = path.relativeTo(namespacePath)
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