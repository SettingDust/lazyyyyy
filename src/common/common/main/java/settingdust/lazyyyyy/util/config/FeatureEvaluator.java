package settingdust.lazyyyyy.util.config;

import settingdust.lazyyyyy.Lazyyyyy;
import settingdust.lazyyyyy.api.config.FeatureDefinition;
import settingdust.lazyyyyy.util.TriState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Utility functions for evaluating feature states.
 * Handles tristate logic and disable conditions.
 */
public final class FeatureEvaluator {
    private FeatureEvaluator() {}
    
    /**
     * Evaluate if a feature is enabled based on state, defaults, and conditions.
     */
    public static boolean isEnabled(
            String featureName,
            Map<String, TriState> states,
            Map<String, Boolean> defaults,
            Map<String, DisableCondition> conditions
    ) {
        TriState state = states.get(featureName);
        
        // Explicit true/false override everything
        if (state == TriState.TRUE) return true;
        if (state == TriState.FALSE) return false;
        
        // Use default and check conditions
        boolean defaultEnabled = defaults.getOrDefault(featureName, false);
        if (!defaultEnabled) return false;
        
        DisableCondition condition = conditions.get(featureName);
        return condition == null || !condition.condition().getAsBoolean();
    }
    
    /**
     * Build default enabled map from definitions.
     */
    public static Map<String, Boolean> buildDefaults(List<FeatureDefinition> definitions) {
        Map<String, Boolean> defaults = new HashMap<>();
        for (FeatureDefinition def : definitions) {
            defaults.put(def.name(), def.defaultEnabled());
        }
        return defaults;
    }
    
    /**
     * Log disabled features with reasons.
     */
    public static void logDisabledFeatures(
            Map<String, TriState> states,
            Map<String, Boolean> defaults,
            Map<String, DisableCondition> conditions
    ) {
        states.forEach((name, state) -> {
            if (!isEnabled(name, states, defaults, conditions)) {
                String reason = getDisableReason(name, state, conditions);
                if (reason != null) {
                    Lazyyyyy.LOGGER.info("{} disabled by {}", name, reason);
                } else {
                    Lazyyyyy.LOGGER.info("{} disabled", name);
                }
            }
        });
    }
    
    private static String getDisableReason(String name, TriState state, Map<String, DisableCondition> conditions) {
        if (state == TriState.FALSE) return "config";
        
        DisableCondition condition = conditions.get(name);
        if (condition != null && condition.condition().getAsBoolean()) {
            return condition.reason();
        }
        
        return null;
    }
    
    /**
     * Disable condition with reason.
     */
    public record DisableCondition(BooleanSupplier condition, String reason) {}
}
