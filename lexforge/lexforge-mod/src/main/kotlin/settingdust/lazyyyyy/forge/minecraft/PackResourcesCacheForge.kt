package settingdust.lazyyyyy.forge.minecraft

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.SimplePackResourcesCache
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.fileVisitor
import kotlin.io.path.name
import kotlin.io.path.visitFileTree
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

val supported = setOf(
    "net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack",
    "net.minecraftforge.resource.PathPackResources"
)

class TreePackResourcesCache(pack: TreeResourcePack, roots: List<Path>) : SimplePackResourcesCache(pack, roots) {
    constructor(root: Path, pack: TreeResourcePack) : this(pack, listOf(root))

    private val allNamespaces = ConcurrentHashMap.newKeySet<String>()

    override fun loadCache() =
        CoroutineScope(Dispatchers.IO + CoroutineName("Dynamic Trees Pack Cache #${pack.packId()}") + CoroutineExceptionHandler { context, throwable ->
            if (throwable is Exception || throwable is Error)
                Lazyyyyy.logger.error("Error loading pack cache in $context", throwable)
        }).launch {
            val time = measureTime {
                val allJobs = ConcurrentHashMap.newKeySet<Job>()
                roots.asFlow().concurrent().collect { root ->
                    val jobs = mutableListOf<Job>()
                    root.visitFileTree(fileVisitor {
                        onPreVisitDirectory { directory, attributes ->
                            val relativePath = root.relativize(directory)
                            jobs.add(launch {
                                if (relativePath.nameCount != 1) return@launch
                                allNamespaces.add(relativePath.name)
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
                                if (relativePath.nameCount < 3)
                                    return@launch
                                val namespace = relativePath.getName(0).name
                                val namespaceRoot = root.resolve(namespace).let {
                                    if (file.isAbsolute) it.toAbsolutePath() else it
                                }
                                for (i in 1 until relativePath.nameCount) {
                                    val directory = relativePath.subpath(0, i)
                                    directoryToFiles
                                        .computeIfAbsent(directory.toString()) { ConcurrentHashMap() }
                                        .put(file, JOINER.join(namespaceRoot.relativize(file)))
                                }
                            })
                            FileVisitResult.CONTINUE
                        }
                    })
                    allJobs.addAll(jobs)
                }
                allJobs.joinAll()
            }
            if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache tree pack ${pack.packId()} in $time")
            else Lazyyyyy.logger.debug("Cache tree pack ${pack.packId()} in $time")
        }

    override fun getNamespaces(type: PackType?) = allNamespaces
}