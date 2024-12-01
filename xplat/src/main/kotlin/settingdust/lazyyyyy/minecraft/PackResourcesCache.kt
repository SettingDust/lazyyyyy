package settingdust.lazyyyyy.minecraft

import com.google.common.base.Joiner
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import settingdust.lazyyyyy.Lazyyyyy
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.fileVisitor
import kotlin.io.path.name
import kotlin.io.path.relativeTo
import kotlin.io.path.visitFileTree
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

object PackResourcesCache {
    val tracked = Collections.synchronizedSet(Collections.newSetFromMap<PackResources>(WeakHashMap()))

    fun track(packResources: PackResources) {
        tracked += packResources
    }

    fun untrack(packResources: PackResources) {
        tracked -= packResources
    }

    fun invalidate() {
        synchronized(tracked) {
            for (resources in tracked.toList()) {
                resources.close()
            }
        }
    }
}

class CachingPackResources(val root: Path, val pack: PackResources) {
    companion object {
        val JOINER = Joiner.on('/')

        private val packTypeByDirectory = PackType.entries.associateByTo(Object2ReferenceOpenHashMap()) { it.directory }
    }

    val files: MutableMap<String, Path> = ConcurrentHashMap()
    val directories: MutableMap<String, Path> = ConcurrentHashMap()
    val directoryToFiles: MutableMap<String, MutableList<Path>> =
        Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap())
    val namespaces: MutableMap<PackType, MutableSet<String>> =
        Collections.synchronizedMap(EnumMap(PackType::class.java))

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val loadingJob = loadCache()

    @OptIn(ExperimentalPathApi::class)
    private fun loadCache() = scope.launch {
        val time = measureTime {
            val jobs = mutableListOf<Job>()
            root.visitFileTree(fileVisitor {
                onPreVisitDirectory { directory, attributes ->
                    jobs.add(launch {
                        val relativePath = root.relativize(directory)
                        val path = JOINER.join(relativePath)
                        directories[path] = directory
                        if (relativePath.nameCount != 2) return@launch
                        val type = packTypeByDirectory[relativePath.first().name] ?: return@launch
                        val namespace = relativePath.name
                        namespaces
                            .getOrPut(type) { Collections.newSetFromMap(ConcurrentHashMap()) }
                            .add(namespace)
                    })
                    FileVisitResult.CONTINUE
                }

                onVisitFile { file, attributes ->
                    jobs.add(launch {
                        val relativePath = root.relativize(file)
                        val pathString = JOINER.join(relativePath)
                        files[pathString] = file
                        directoryToFiles.getOrPut("") { ObjectLists.synchronize(ObjectArrayList()) }.add(file)
                        if (relativePath.parent != null) {
                            for (i in 1..relativePath.parent.nameCount) {
                                val path = JOINER.join(relativePath.parent.subpath(0, i))
                                directoryToFiles.getOrPut(path) { ObjectLists.synchronize(ObjectArrayList()) }.add(file)
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

    fun getPath(vararg paths: String) = when (paths.size) {
        0 -> root.fileSystem.getPath("")
        1 -> root.fileSystem.getPath(paths[0])
        else -> root.fileSystem.getPath(paths[0], *paths.drop(1).toTypedArray())
    }

    fun getNamespaces(type: PackType) = runBlocking(scope.coroutineContext) {
        loadingJob.join()
        namespaces[type] ?: emptySet()
    }

    fun getResource(
        path: Path
    ) = runBlocking(scope.coroutineContext) {
        loadingJob.join()
        val path = files[JOINER.join(path)] ?: return@runBlocking null
        try {
            IoSupplier.create(path)
        } catch (_: IOException) {
            null
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun listResources(type: PackType, namespace: String, prefix: String, output: PackResources.ResourceOutput) =
        runBlocking(scope.coroutineContext) {
            loadingJob.join()
            val dirName = "${type.directory}/$namespace/$prefix"
            val dir = directories[dirName] ?: return@runBlocking
            val filesInDir = directoryToFiles[dirName] ?: return@runBlocking
            for (path in filesInDir) {
                val relativePath = path.relativeTo(dir)
                val relativeString = JOINER.join(relativePath)
                val location = ResourceLocation.tryBuild(namespace, relativeString)
                if (location != null) {
                    output.accept(location, IoSupplier.create(path))
                } else {
                    Lazyyyyy.logger.warn("Invalid path in pack: $namespace:$path, ignoring")
                }
            }
        }
}