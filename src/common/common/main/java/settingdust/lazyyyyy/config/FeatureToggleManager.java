package settingdust.lazyyyyy.config;

import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Generic feature toggle manager with tristate support and conditional logic.
 * Can be reused across different configuration systems.
 *
 * @param <T> the type of feature identifier (typically String)
 */
public class FeatureToggleManager<T> {
    private final Map<T, Boolean> defaultEnabled;
    private final Map<T, FeatureConfig> featureConfigs = new HashMap<>();
    private final Map<T, TriState> currentStates = new HashMap<>();
    private final Logger logger;

    /**
     * Create a feature toggle manager.
     *
     * @param defaultEnabled map of default enabled states
     * @param logger logger for outputting disable information
     */
    public FeatureToggleManager(Map<T, Boolean> defaultEnabled, Logger logger) {
        this.defaultEnabled = defaultEnabled;
        this.logger = logger;
    }

    /**
     * Register a feature with disable condition and reason.
     *
     * @param featureId the feature identifier
     * @param disableCondition condition that disables the feature
     * @param disableReason reason identifier for logging
     */
    public void registerDisableCondition(T featureId, BooleanSupplier disableCondition, String disableReason) {
        featureConfigs.put(featureId, new FeatureConfig(disableCondition, disableReason));
    }

    /**
     * Set the tristate for a feature.
     *
     * @param featureId the feature identifier
     * @param state the tristate value
     */
    public void setState(T featureId, TriState state) {
        currentStates.put(featureId, state);
    }

    /**
     * Check if a feature is enabled.
     *
     * @param featureId the feature identifier
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(T featureId) {
        return evaluateState(featureId).enabled;
    }

    /**
     * Log all disabled features with their reasons.
     */
    public void logDisabledFeatures() {
        currentStates.forEach((featureId, state) -> {
            EvaluationResult result = evaluateState(featureId);
            if (!result.enabled) {
                if (result.disableSource != null) {
                    logger.info("{} disabled by {}", featureId, result.disableSource);
                } else {
                    logger.info("{} disabled", featureId);
                }
            }
        });
    }

    /**
     * Evaluate the enabled state of a feature.
     *
     * @param featureId the feature identifier
     * @return evaluation result with enabled status and disable source
     */
    private EvaluationResult evaluateState(T featureId) {
        TriState state = currentStates.get(featureId);

        if (state == TriState.TRUE) {
            return new EvaluationResult(true, null);
        }
        if (state == TriState.FALSE) {
            return new EvaluationResult(false, "config");
        }

        // Use default value and check condition
        boolean enabled = defaultEnabled.getOrDefault(featureId, true);

        if (enabled) {
            FeatureConfig config = featureConfigs.get(featureId);
            if (config != null && config.shouldDisable()) {
                return new EvaluationResult(false, config.getDisableReason());
            }
        }

        return new EvaluationResult(enabled, null);
    }

    /**
     * Result of feature state evaluation.
     */
    private record EvaluationResult(boolean enabled, String disableSource) {}

    /**
     * Initialize all features with default tristate.
     */
    public void initializeDefaults() {
        for (T featureId : defaultEnabled.keySet()) {
            currentStates.putIfAbsent(featureId, TriState.DEFAULT);
        }
    }

    /**
     * Get current states map for serialization.
     *
     * @return map of feature states
     */
    public Map<T, TriState> getStates() {
        return currentStates;
    }
}
