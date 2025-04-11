package settingdust.lazyyyyy

import com.moulberry.mixinconstraints.ConstraintsMixinPlugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.logging.log4j.LogManager
import org.embeddedt.modernfix.core.ModernFixMixinPlugin
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
class LazyyyyyMixinPlugin : ConstraintsMixinPlugin() {
    companion object {
        private var firstLoad = true
        private var firstApply = true
    }

    lateinit var mixinPackage: String
        private set

    val defaultConfig = mapOf<String, Boolean>(
        "debug" to false,
        "async_model_baking" to false,
        "axiom.async_check_commercial" to true,
        "entity_sound_features.async_sound_events" to true,
        "lazy_entity_renderers" to true,
        "moremcmeta.avoid_duplicate_sprites" to true,
        "toomanyplayers.async_networking" to true,
        "yacl.lazy_animated_image" to true,
        "kiwi.faster_annotation" to true,
        "pack_resources_cache" to true,
        "avoid_redundant_list_resources" to true
    )

    val conflictings = mapOf<String, MutableSet<String>>(
        "async_model_baking" to mutableSetOf("duclib")
    )

    var config = defaultConfig.toMutableMap()
        private set
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val logger = LogManager.getLogger()

    init {
        val configPath = Path("./config/lazyyyyy.mixins.json")
        runCatching { configPath.createParentDirectories() }
        runCatching { configPath.createFile() }
        runCatching {
            config = defaultConfig.toMutableMap()
            config.putAll(json.decodeFromStream(configPath.inputStream()))
        }

        if (!isPlatformServiceFailedToLoad) {
            for ((feature, mods) in conflictings) {
                val conflicting = mods.firstOrNull { PlatformService.isModLoaded(it) } ?: continue
                logger.warn("Disabling feature $feature due to conflicting mod $conflicting")
                config[feature] = false
            }
        }
        json.encodeToStream(config, configPath.outputStream())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onLoad(mixinPackage: String) {
        super.onLoad(mixinPackage)
        this.mixinPackage = mixinPackage
        if (firstLoad) {
            firstLoad = false
            if (config["debug"] == true) {
                DebugProbes.install()
            }
        }
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (firstApply) {
            firstApply = false
            if (config.any { it.key.startsWith("pack_resources_cache") && it.value }) {
                try {
                    ModernFixMixinPlugin.instance.config.permanentlyDisabledMixins["perf.resourcepacks.ReloadableResourceManagerMixin"] =
                        "lazyyyyy"
                    ModernFixMixinPlugin.instance.config.permanentlyDisabledMixins["perf.resourcepacks.ForgePathPackResourcesMixin"] =
                        "lazyyyyy"
                    logger.info("Disabled ModernFix resourcepacks cache")
                } catch (_: NoClassDefFoundError) {
                }
            }

            if (config.any { it.key.startsWith("lazy_entity_renderers") && it.value }) {
                try {
                    forge.me.thosea.badoptimizations.other.Config.enable_entity_renderer_caching = false
                    forge.me.thosea.badoptimizations.other.Config.enable_block_entity_renderer_caching = false
                    logger.info("Disabled BadOptimizations `enable_entity_renderer_caching` and `enable_block_entity_renderer_caching`")
                } catch (_: NoClassDefFoundError) {
                }
                try {
                    fabric.me.thosea.badoptimizations.other.Config.enable_entity_renderer_caching = false
                    fabric.me.thosea.badoptimizations.other.Config.enable_block_entity_renderer_caching = false
                    logger.info("Disabled BadOptimizations `enable_entity_renderer_caching` and `enable_block_entity_renderer_caching`")
                } catch (_: NoClassDefFoundError) {
                }
            }
        }

        if (isPlatformServiceFailedToLoad) return false

        if (!mixinClassName.startsWith(mixinPackage)) return super.shouldApplyMixin(targetClassName, mixinClassName)
        val relativeName = mixinClassName.removePrefix("${mixinPackage}.").removePrefix("forge.")
        val disabled =
            config.asSequence()
                .any { entry -> relativeName.startsWith(entry.key) && !entry.value }
        if (disabled) {
            logger.info("Disabled '$relativeName' due to config")
        }
        return !disabled && super.shouldApplyMixin(targetClassName, mixinClassName)
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
        when (mixinClassName) {
            "settingdust.lazyyyyy.mixin.entity_texture_features.async_compat.ETFManagerMixin" -> {

            }
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo)
    }
}