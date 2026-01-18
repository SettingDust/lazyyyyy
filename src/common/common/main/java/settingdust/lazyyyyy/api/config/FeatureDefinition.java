package settingdust.lazyyyyy.api.config;

/**
 * Definition of a feature with metadata.
 * Immutable data structure containing feature name, default state, and descriptions.
 */
public record FeatureDefinition(
        String name,
        boolean defaultEnabled,
        String[] descriptions
) {
    /**
     * Create a feature definition with default enabled.
     */
    public static FeatureDefinition enabled(String name, String... descriptions) {
        return new FeatureDefinition(name, true, descriptions);
    }

    /**
     * Create a feature definition with default disabled.
     */
    public static FeatureDefinition disabled(String name, String... descriptions) {
        return new FeatureDefinition(name, false, descriptions);
    }
}
