package settingdust.lazyyyyy.config;

import java.util.function.BooleanSupplier;

/**
 * Feature configuration with disable condition and reason.
 * Can be used to define conditional feature toggles.
 */
public class FeatureConfig {
    private final BooleanSupplier disableCondition;
    private final String disableReason;

    /**
     * Create a feature config with disable condition and reason.
     *
     * @param disableCondition condition that disables the feature (returns true when should be disabled)
     * @param disableReason reason identifier for logging
     */
    public FeatureConfig(BooleanSupplier disableCondition, String disableReason) {
        this.disableCondition = disableCondition;
        this.disableReason = disableReason;
    }

    /**
     * Check if the disable condition is met.
     *
     * @return true if should be disabled, false otherwise
     */
    public boolean shouldDisable() {
        return disableCondition != null && disableCondition.getAsBoolean();
    }

    /**
     * Get the disable reason identifier.
     *
     * @return reason identifier, or null if not specified
     */
    public String getDisableReason() {
        return disableReason;
    }

    /**
     * Check if this feature has a disable condition.
     *
     * @return true if has condition, false otherwise
     */
    public boolean hasCondition() {
        return disableCondition != null;
    }
}
