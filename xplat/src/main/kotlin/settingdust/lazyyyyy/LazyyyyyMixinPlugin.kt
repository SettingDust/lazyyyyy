package settingdust.lazyyyyy

import com.moulberry.mixinconstraints.ConstraintsMixinPlugin
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
class LazyyyyyMixinPlugin : ConstraintsMixinPlugin() {
    lateinit var mixinPackage: String
        private set

    val defaultConfig = mapOf<String, Boolean>(
        // Meanness since the renderers are lazy
        "async_model_baking" to false,
        "axiom.async_check_commercial" to true,
        "entity_sound_features.async_sound_events" to true,
        "lazy_entity_renderers" to true,
        "moremcmeta.avoid_duplicate_sprites" to true,
        "toomanyplayers.async_networking" to true,
        "yacl.lazy_animated_image" to true,
        "kiwi.faster_annotation" to true,
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
        json.encodeToStream(config, configPath.outputStream())
    }

    override fun onLoad(mixinPackage: String) {
        super.onLoad(mixinPackage)
        this.mixinPackage = mixinPackage
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (!mixinClassName.startsWith(mixinPackage)) return super.shouldApplyMixin(targetClassName, mixinClassName)
        val relativeName = mixinClassName.removePrefix("${mixinPackage}.")
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