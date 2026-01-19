package settingdust.lazyyyyy.config;

import com.google.common.base.Suppliers;
import settingdust.lazyyyyy.Lazyyyyy;
import settingdust.lazyyyyy.api.config.FeatureConfig;
import settingdust.lazyyyyy.api.config.FeatureDefinition;
import settingdust.lazyyyyy.util.LoaderAdapter;
import settingdust.lazyyyyy.util.TriState;
import settingdust.lazyyyyy.util.config.ConfigIO;
import settingdust.lazyyyyy.util.config.FeatureEvaluator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Early configuration for lazyyyyy features.
 * Singleton pattern with static feature definitions and instance state.
 */
public class LazyyyyyEarlyConfig implements FeatureConfig {
    private static final Path CONFIG_PATH = LoaderAdapter.get().getConfigDirectory().resolve("lazyyyyy/early.properties");

    // Static feature definitions
    private static final List<FeatureDefinition> FEATURES = List.of(
            FeatureDefinition.enabled("faster_mixin",
                    "Optimizes mixin configuration loading for faster startup"),
            FeatureDefinition.enabled("faster_module_resolver",
                    "Accelerates Java module resolution (Java 17-21)")
    );
    
    private static final String[] HEADER = new String[]{
            "Lazyyyyy Early Configuration",
            "============================",
            "",
            "This file controls early-stage features that load before Minecraft."
    };

    private static final Supplier<LazyyyyyEarlyConfig> INSTANCE = Suppliers.memoize(LazyyyyyEarlyConfig::new);

    // Instance state
    private final Map<String, TriState> states = new HashMap<>();
    private final Map<String, Boolean> defaults;
    private final Map<String, FeatureEvaluator.DisableCondition> conditions = new HashMap<>();

    private LazyyyyyEarlyConfig() {
        this.defaults = FeatureEvaluator.buildDefaults(FEATURES);
        // Initialize with default states
        for (FeatureDefinition def : FEATURES) {
            states.put(def.name(), TriState.DEFAULT);
        }
    }

    public static LazyyyyyEarlyConfig instance() {
        return INSTANCE.get();
    }

    /**
     * Load configuration states from file.
     * Must be called explicitly to load user configuration.
     */
    public void load() {
        try {
            Map<String, TriState> loadedStates = ConfigIO.load(this);
            states.clear();
            states.putAll(loadedStates);
            FeatureEvaluator.logDisabledFeatures(states, defaults, conditions);
        } catch (IOException e) {
            Lazyyyyy.LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
        }
    }

    @Override
    public void registerDisableCondition(String featureName, BooleanSupplier condition, String reason) {
        conditions.put(featureName, new FeatureEvaluator.DisableCondition(condition, reason));
    }

    @Override
    public boolean isFeatureEnabled(String featureName) {
        return FeatureEvaluator.isEnabled(featureName, states, defaults, conditions);
    }

    @Override
    public List<FeatureDefinition> getDefinitions() {
        return FEATURES;
    }

    @Override
    public Map<String, TriState> getStates() {
        return states;
    }

    @Override
    public Map<String, Boolean> getDefaults() {
        return defaults;
    }

    @Override
    public Map<String, FeatureEvaluator.DisableCondition> getConditions() {
        return conditions;
    }

    @Override
    public Path getFilePath() {
        return CONFIG_PATH;
    }

    @Override
    public String[] getFileHeader() {
        return HEADER;
    }
}
