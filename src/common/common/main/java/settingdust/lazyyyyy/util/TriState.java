package settingdust.lazyyyyy.util;

/**
 * Tristate for configuration values.
 * Provides three states: explicitly enabled, explicitly disabled, or use default behavior.
 */
public enum TriState {
    /**
     * Explicitly enabled - overrides all conditions and defaults.
     */
    TRUE,
    
    /**
     * Explicitly disabled - overrides all conditions and defaults.
     */
    FALSE,
    
    /**
     * Use default value and apply conditions.
     */
    DEFAULT;
    
    /**
     * Convert to boolean with fallback logic.
     *
     * @param defaultValue the default value when state is DEFAULT
     * @return true if enabled, false if disabled
     */
    public boolean asBoolean(boolean defaultValue) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case DEFAULT -> defaultValue;
        };
    }
    
    /**
     * Check if this state is explicitly set (TRUE or FALSE).
     *
     * @return true if explicit, false if DEFAULT
     */
    public boolean isExplicit() {
        return this != DEFAULT;
    }
}
