package settingdust.lazyyyyy.api.config;

import settingdust.lazyyyyy.util.TriState;
import settingdust.lazyyyyy.util.config.FeatureEvaluator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Interface for feature configuration.
 * Provides access to feature definitions, states, and file information.
 */
public interface FeatureConfig {
    /**
     * Get all feature definitions.
     */
    List<FeatureDefinition> getDefinitions();
    
    /**
     * Get current feature states.
     */
    Map<String, TriState> getStates();
    
    /**
     * Get default enabled states.
     */
    Map<String, Boolean> getDefaults();
    
    /**
     * Get disable conditions.
     */
    Map<String, FeatureEvaluator.DisableCondition> getConditions();
    
    /**
     * Get configuration file path.
     */
    Path getFilePath();
    
    /**
     * Get file header description lines.
     */
    String[] getFileHeader();
    
    /**
     * Register a disable condition for a feature.
     * 
     * @param featureName the feature name
     * @param condition condition that disables the feature (returns true when should be disabled)
     * @param reason reason identifier for logging
     */
    void registerDisableCondition(String featureName, BooleanSupplier condition, String reason);
    
    /**
     * Check if a feature is enabled.
     * 
     * @param featureName the feature name
     * @return true if enabled, false otherwise
     */
    boolean isFeatureEnabled(String featureName);
}
