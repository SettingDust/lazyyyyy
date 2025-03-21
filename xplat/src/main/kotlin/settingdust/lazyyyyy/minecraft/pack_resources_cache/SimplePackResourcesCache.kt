package settingdust.lazyyyyy.minecraft.pack_resources_cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.minecraft.pack_resources_cache.CachingStrategy.PackRoot
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import settingdust.lazyyyyy.util.withCoroutineNameSuffix
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

typealias FileConsumer = (Path) -> Unit?

open class SimplePackResourcesCache(pack: PackResources, roots: List<Path>) : PackResourcesCache(pack, roots) {
    constructor(root: Path, pack: PackResources) : this(pack, listOf(root))

    var namespaces: MutableMap<PackType, CompletableDeferred<Set<String>>> = ConcurrentHashMap()

    init {
        scope.launch { loadCache() }
    }

    private suspend fun consumeRoot(
        root: Path,
        namespaces: ConcurrentHashMap<PackType, MutableSet<String>>
    ) {
        val strategy = PackRoot(root, null)
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#root/$root] caching") }
        root.listDirectoryEntries().asFlow().concurrent().collect { path ->
            val relativePath = root.relativize(path)
            if (path.isDirectory()) {
                val firstPath = relativePath.firstOrNull()
                val packType = packTypeByDirectory[firstPath?.name] ?: return@collect
                consumePackType(packType, path, PackRoot(root, path), namespaces)
            } else {
                consumeFile(path, strategy)
            }
        }
        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#root/$root] cached") }
    }

    private suspend fun PackResourcesCache.consumePackType(
        type: PackType,
        directory: Path,
        strategy: CachingStrategy,
        namespaces: MutableMap<PackType, MutableSet<String>>
    ) {
        val entries = directory.listDirectoryEntries()
        coroutineScope {
            withContext(withCoroutineNameSuffix(" / ${type.directory}")) {
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] caching") }
                joinAll(
                    launch {
                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/namespaces] caching") }
                        val toPut = entries.mapTo(mutableSetOf()) { it.name }
                        namespaces.computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }.addAll(toPut)
                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/namespaces] cached $toPut") }
                    },
                    launch {
                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] caching") }
                        val directoryToFiles = ConcurrentHashMap<String, MutableMap<Path, Deferred<String>>>()
                        entries.asFlow().concurrent().collect { path ->
                            if (path.isDirectory()) {
                                consumeResourceDirectory(path, directoryToFiles, strategy)
                            } else {
                                consumeFile(path, strategy)
                            }
                        }
                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/directoryToFiles] caching") }
                        for ((path, files) in directoryToFiles) {
                            this@SimplePackResourcesCache.directoryToFiles[path]!!.complete(files.mapValues { it.value.await() })
                        }
                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/directoryToFiles] cached") }

                        Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type/entries] cached") }
                    }
                )
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}#packType/$type] cached") }
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun CoroutineScope.loadCache() =
        withContext(CoroutineName("Simple pack cache #${pack.packId()}")) {
            val time = measureTime {
                for (type in PackType.entries) {
                    namespaces.computeIfAbsent(type) { CompletableDeferred() }
                }

                val namespaces = ConcurrentHashMap<PackType, MutableSet<String>>()

                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] caching") }
                roots.asFlow().concurrent().collect { root -> consumeRoot(root, namespaces) }

                for ((type, deferred) in this@SimplePackResourcesCache.namespaces) {
                    deferred.complete(namespaces[type] ?: emptySet())
                }
                allCompleted.complete()
                Lazyyyyy.DebugLogging.packCache.whenDebug { info("[${pack.packId()}] cached") }
            }
            if (time >= 500.milliseconds) Lazyyyyy.logger.warn("Cache pack ${pack.packId()} in $time")
            else Lazyyyyy.logger.debug("Cache pack ${pack.packId()} in $time")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNamespaces(type: PackType?): Set<String> {
        if (type == null) return emptySet()
        val deferred = namespaces.computeIfAbsent(type) { CompletableDeferred() }
        return (if (deferred.isCompleted) deferred.getCompleted()
        else runBlocking { namespaces[type]?.await() }) ?: emptySet()
    }
}