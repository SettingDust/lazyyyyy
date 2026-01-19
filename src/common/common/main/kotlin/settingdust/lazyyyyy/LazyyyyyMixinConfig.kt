package settingdust.lazyyyyy

import org.apache.logging.log4j.Logger
import settingdust.lazyyyyy.api.config.FeatureConfig
import settingdust.lazyyyyy.api.config.FeatureDefinition
import settingdust.lazyyyyy.util.TriState
import settingdust.lazyyyyy.util.config.ConfigIO
import settingdust.lazyyyyy.util.config.FeatureEvaluator
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Configuration manager for Lazyyyyy mixin features.
 * Implements FeatureConfig interface with properties-based persistence.
 */
class LazyyyyyMixinConfig(private val logger: Logger) : FeatureConfig {
    private val configPath = Path("./config/lazyyyyy.mixins.properties")
    
    private val features = mutableListOf<FeatureDefinition>()
    private val states = mutableMapOf<String, TriState>()
    private val defaults = mutableMapOf<String, Boolean>()
    private val conditions = mutableMapOf<String, FeatureEvaluator.DisableCondition>()
    
    private val header = arrayOf(
        "Lazyyyyy Mixin Configuration",
        "============================",
        "",
        "This file controls mixin features that enhance Minecraft performance and compatibility."
    )

    /**
     * Register a feature definition.
     */
    fun registerFeature(definition: FeatureDefinition) {
        features.add(definition)
        defaults[definition.name()] = definition.defaultEnabled()
        states[definition.name()] = TriState.DEFAULT
    }

    /**
     * Register multiple feature definitions.
     */
    fun registerFeatures(vararg definitions: FeatureDefinition) {
        definitions.forEach { registerFeature(it) }
    }

    /**
     * Register a feature with name, default state, and description.
     */
    fun registerFeature(name: String, defaultEnabled: Boolean, vararg descriptions: String) {
        val definition = FeatureDefinition(name, defaultEnabled, descriptions)
        registerFeature(definition)
    }

    /**
     * Register feature default directly.
     */
    fun registerDefault(name: String, defaultEnabled: Boolean) {
        defaults[name] = defaultEnabled
    }

    /**
     * Load configuration from file and apply conflict detection.
     */
    fun load(): Map<String, Boolean> {
        try {
            val loadedStates = ConfigIO.load(this)
            states.clear()
            states.putAll(loadedStates)
            FeatureEvaluator.logDisabledFeatures(states, defaults, conditions)
        } catch (e: IOException) {
            logger.error("Failed to load config from {}", configPath, e)
        }
        
        // Build and cache config map
        config = states.mapValues { (name, state) ->
            FeatureEvaluator.isEnabled(name, states, defaults, conditions)
        }
        
        return config
    }

    /**
     * Check if a feature should be applied based on its name prefix.
     * Returns true if the feature is enabled, false otherwise.
     * Logs the disable reason if feature is disabled.
     */
    fun shouldApplyFeature(featureName: String): Boolean {
        // Find matching config entry by prefix
        val matchedEntry = config.entries.firstOrNull { (key, _) ->
            featureName.startsWith(key)
        }

        if (matchedEntry == null) return true

        val (configKey, enabled) = matchedEntry
        if (!enabled) {
            // Check if there's a disable condition with reason
            val condition = conditions[configKey]
            if (condition != null && condition.condition.asBoolean) {
                logger.info("Disabled '$featureName': ${condition.reason}")
            } else {
                logger.info("Disabled '$featureName' due to config")
            }
            return false
        }

        return true
    }

    private lateinit var config: Map<String, Boolean>

    override fun getDefinitions(): List<FeatureDefinition> = features

    override fun getStates(): Map<String, TriState> = states

    override fun getDefaults(): Map<String, Boolean> = defaults

    override fun getConditions(): Map<String, FeatureEvaluator.DisableCondition> = conditions

    override fun getFilePath(): Path = configPath

    override fun getFileHeader(): Array<String> = header

    companion object {
        // Feature name constants will be added here
    }
}
