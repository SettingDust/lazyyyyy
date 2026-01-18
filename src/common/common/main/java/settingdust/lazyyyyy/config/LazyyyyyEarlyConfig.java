package settingdust.lazyyyyy.config;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settingdust.lazyyyyy.util.LoaderAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class LazyyyyyEarlyConfig {
    private static final Logger LOGGER = LogManager.getLogger("Lazyyyyy");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = LoaderAdapter.get().getConfigDirectory().resolve("lazyyyyy/early.json");

    private static final Supplier<LazyyyyyEarlyConfig> INSTANCE = Suppliers.memoize(() -> {
        try {
            return load();
        } catch (IOException e) {
            LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
            return createDefault();
        }
    });

    // Default enabled state for features
    private static final Map<String, Boolean> DEFAULT_ENABLED = Map.of(
            "faster_mixin", true,
            "faster_module_resolver", true
    );
    
    // Feature toggle manager
    private static final FeatureToggleManager<String> FEATURE_MANAGER = new FeatureToggleManager<>(DEFAULT_ENABLED, LOGGER);

    private Map<String, TriState> features;

    public static LazyyyyyEarlyConfig instance() {
        return INSTANCE.get();
    }

    /**
     * Register a feature definition with disable condition and reason.
     *
     * @param featureName the feature name
     * @param disableCondition condition that disables the feature (returns true when should be disabled)
     * @param disableReason reason identifier for logging
     */
    public static void registerDisableCondition(String featureName, BooleanSupplier disableCondition, String disableReason) {
        FEATURE_MANAGER.registerDisableCondition(featureName, disableCondition, disableReason);
    }

    private static LazyyyyyEarlyConfig load() throws IOException {
        LazyyyyyEarlyConfig config = Files.exists(CONFIG_PATH)
                ? loadFromFile()
                : createDefaultAndSave();
        
        // Apply states and log
        config.features.forEach(FEATURE_MANAGER::setState);
        FEATURE_MANAGER.logDisabledFeatures();
        
        return config;
    }

    private static LazyyyyyEarlyConfig loadFromFile() throws IOException {
        String json = Files.readString(CONFIG_PATH);
        LazyyyyyEarlyConfig config = GSON.fromJson(json, LazyyyyyEarlyConfig.class);
        LOGGER.debug("Loaded config from {}", CONFIG_PATH);
        return config != null ? config : createDefault();
    }

    private static LazyyyyyEarlyConfig createDefaultAndSave() throws IOException {
        LOGGER.info("Config file not found at {}, creating default", CONFIG_PATH);
        LazyyyyyEarlyConfig config = createDefault();
        config.save();
        return config;
    }

    private static LazyyyyyEarlyConfig createDefault() {
        LazyyyyyEarlyConfig config = new LazyyyyyEarlyConfig();
        
        // Initialize feature manager
        FEATURE_MANAGER.initializeDefaults();
        config.features = FEATURE_MANAGER.getStates();
        
        return config;
    }

    private void save() throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        String json = GSON.toJson(this);
        Files.writeString(CONFIG_PATH, json);
        LOGGER.debug("Saved config to {}", CONFIG_PATH);
    }

    /**
     * Check if a feature is enabled.
     *
     * @param featureName the feature name
     * @return true if enabled, false otherwise
     */
    public boolean isFeatureEnabled(String featureName) {
        return FEATURE_MANAGER.isEnabled(featureName);
    }
}
