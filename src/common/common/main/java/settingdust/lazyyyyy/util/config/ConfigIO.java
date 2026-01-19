package settingdust.lazyyyyy.util.config;

import settingdust.lazyyyyy.api.config.FeatureConfig;
import settingdust.lazyyyyy.api.config.FeatureDefinition;
import settingdust.lazyyyyy.util.TriState;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility functions for loading and saving feature configurations.
 * Pure functions without state or dependencies.
 */
public final class ConfigIO {
    private ConfigIO() {}
    
    /**
     * Load feature states from properties file.
     * Returns default states if file doesn't exist.
     */
    public static Map<String, TriState> load(FeatureConfig config) throws IOException {
        Path path = config.getFilePath();
        
        if (!Files.exists(path)) {
            Map<String, TriState> defaults = createDefaultStates(config);
            save(config, defaults);
            return defaults;
        }
        
        return loadFromFile(path, config);
    }
    
    private static Map<String, TriState> loadFromFile(Path path, FeatureConfig config) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        
        Map<String, TriState> states = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            states.put(key, parseTriState(properties.getProperty(key)));
        }
        
        // Ensure all features exist
        for (FeatureDefinition def : config.getDefinitions()) {
            states.putIfAbsent(def.name(), TriState.DEFAULT);
        }
        
        return states;
    }
    
    /**
     * Save feature states to properties file with comments.
     */
    public static void save(FeatureConfig config, Map<String, TriState> states) throws IOException {
        Path path = config.getFilePath();
        Files.createDirectories(path.getParent());
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writeHeader(writer, config);
            writeFeatures(writer, config, states);
        }
    }
    
    private static void writeHeader(BufferedWriter writer, FeatureConfig config) throws IOException {
        for (String line : config.getFileHeader()) {
            writer.write("# " + line + "\n");
        }
        writer.write("#\n");
        writer.write("# Each feature can be set to: true, false, or default\n");
        writer.write("# - true: Force enable the feature\n");
        writer.write("# - false: Force disable the feature\n");
        writer.write("# - default: Use the default value (may be affected by conditions)\n");
        writer.write("#\n\n");
    }
    
    private static void writeFeatures(BufferedWriter writer, FeatureConfig config, Map<String, TriState> states) throws IOException {
        boolean first = true;
        for (FeatureDefinition def : config.getDefinitions()) {
            if (!first) writer.write("\n");
            first = false;
            
            // Write descriptions
            for (String desc : def.descriptions()) {
                writer.write("# " + desc + "\n");
            }
            
            // Always show what DEFAULT means for this feature
            String defaultInfo = getDefaultStateInfo(def.name(), config);
            writer.write("# " + defaultInfo + "\n");
            
            String value = triStateToString(states.getOrDefault(def.name(), TriState.DEFAULT));
            writer.write(def.name() + "=" + value + "\n");
        }
    }
    
    private static String getDefaultStateInfo(String featureName, FeatureConfig config) {
        boolean defaultEnabled = config.getDefaults().getOrDefault(featureName, false);
        var condition = config.getConditions().get(featureName);
        
        if (!defaultEnabled) {
            return "DEFAULT - disabled by default";
        }
        
        if (condition != null && condition.condition().getAsBoolean()) {
            return "DEFAULT - disabled by " + condition.reason();
        }
        
        return "DEFAULT - enabled";
    }
    
    private static Map<String, TriState> createDefaultStates(FeatureConfig config) {
        Map<String, TriState> states = new HashMap<>();
        for (FeatureDefinition def : config.getDefinitions()) {
            states.put(def.name(), TriState.DEFAULT);
        }
        return states;
    }
    
    private static TriState parseTriState(String value) {
        if (value == null) return TriState.DEFAULT;
        return switch (value.toLowerCase()) {
            case "true" -> TriState.TRUE;
            case "false" -> TriState.FALSE;
            default -> TriState.DEFAULT;
        };
    }
    
    private static String triStateToString(TriState state) {
        return switch (state) {
            case TRUE -> "true";
            case FALSE -> "false";
            case DEFAULT -> "default";
        };
    }
}
