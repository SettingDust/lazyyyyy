package settingdust.lazyyyyy

import com.moulberry.mixinconstraints.ConstraintsMixinPlugin
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.logging.log4j.LogManager
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
class LazyyyyyMixinPlugin : ConstraintsMixinPlugin() {
    lateinit var mixinPackage: String
        private set
    var config = mutableMapOf<String, Boolean>(
        "async_entity_renderers" to false,
        "async_model_baking" to true,
        "yacl.lazy_animated_image" to true,
        "kiwi.faster_annotation" to true
    )
        private set
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    val logger = LogManager.getLogger()

    init {
        val configPath = Path("./config/lazyyyyy.mixins.json")
        runCatching { configPath.createParentDirectories() }
        runCatching { configPath.createFile() }
        runCatching { config = json.decodeFromStream(configPath.inputStream()) }
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
        if (disabled) { logger.info("Disabled '$relativeName' due to config") }
        return !disabled && super.shouldApplyMixin(targetClassName, mixinClassName)
    }
}