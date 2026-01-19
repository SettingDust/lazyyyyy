package settingdust.lazyyyyy.fabric.config

import net.fabricmc.loader.api.FabricLoader
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.api.config.FeatureConfig
import settingdust.lazyyyyy.api.config.FeatureDefinition
import settingdust.lazyyyyy.util.LoaderAdapter
import settingdust.lazyyyyy.util.TriState
import settingdust.lazyyyyy.util.config.ConfigIO
import settingdust.lazyyyyy.util.config.FeatureEvaluator
import java.io.IOException
import java.nio.file.Path

/**
 * Early configuration for Fabric-specific features.
 * Singleton pattern with static feature definitions and instance state.
 */
class FabricEarlyConfig private constructor() : FeatureConfig {
    private val states = mutableMapOf<String, TriState>()
    private val defaults = FeatureEvaluator.buildDefaults(FEATURES)
    private val conditions = mutableMapOf<String, FeatureEvaluator.DisableCondition>()

    init {
        // Initialize with default states
        FEATURES.forEach { def ->
            states[def.name()] = TriState.DEFAULT
        }

        registerDisableCondition(
            BETTER_LOG4J_CONFIG_EARLIER_INIT,
            { !FabricLoader.getInstance().isModLoaded("better_log4j_config") },
            "better_log4j_config isn't exists"
        )
    }

    /**
     * Load configuration states from file.
     * Must be called explicitly to load user configuration.
     */
    fun load() {
        try {
            val loadedStates = ConfigIO.load(this)
            states.clear()
            states.putAll(loadedStates)
            FeatureEvaluator.logDisabledFeatures(states, defaults, conditions)
        } catch (e: IOException) {
            Lazyyyyy.LOGGER.error("Failed to load config from {}", CONFIG_PATH, e)
        }
    }

    override fun getDefinitions(): List<FeatureDefinition> = FEATURES

    override fun getStates(): Map<String, TriState> = states

    override fun getDefaults(): Map<String, Boolean> = defaults

    override fun getConditions(): Map<String, FeatureEvaluator.DisableCondition> = conditions

    override fun getFilePath(): Path = CONFIG_PATH

    override fun getFileHeader(): Array<String> = HEADER

    companion object {
        private val CONFIG_PATH = LoaderAdapter.get().configDirectory.resolve("lazyyyyy/fabric-early.properties")

        // Feature names
        const val BETTER_LOG4J_CONFIG_EARLIER_INIT = "better_log4j_config.earlier_init"

        // Static feature definitions
        private val FEATURES = listOf(
            FeatureDefinition.enabled(
                BETTER_LOG4J_CONFIG_EARLIER_INIT,
                "Enables earlier initialization of better_log4j_config for improved logging"
            )
        )

        private val HEADER = arrayOf(
            "Lazyyyyy Fabric Early Configuration",
            "====================================",
            "",
            "This file controls Fabric-specific early-stage features."
        )

        private val INSTANCE by lazy { FabricEarlyConfig() }

        @JvmStatic
        fun instance(): FabricEarlyConfig = INSTANCE
    }
}
