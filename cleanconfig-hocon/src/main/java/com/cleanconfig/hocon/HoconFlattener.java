package com.cleanconfig.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class that flattens a Typesafe {@link Config} object into a {@code Map<String, String>}.
 *
 * <p>The flattening rules are:</p>
 * <ul>
 *   <li>Nested objects are joined with dots: {@code server.port}</li>
 *   <li>Arrays flatten to indexed keys: {@code items.0}, {@code items.1}</li>
 *   <li>Booleans and numbers are converted via {@code toString()}</li>
 *   <li>Strings use their raw unwrapped value</li>
 *   <li>Null values are omitted from the resulting map</li>
 *   <li>Duration values are rendered as HOCON strings (e.g., "30s", "5m")</li>
 * </ul>
 *
 * <p>This class is stateless and all methods are static. It cannot be instantiated.</p>
 *
 * @since 0.1.0
 */
public final class HoconFlattener {

    private HoconFlattener() {
        // Utility class — prevent instantiation
    }

    /**
     * Flattens a resolved {@link Config} into an unmodifiable {@code Map<String, String>}.
     *
     * @param config the resolved Typesafe Config to flatten (must not be null)
     * @return an unmodifiable map of dot-separated keys to string values
     * @throws NullPointerException if config is null
     */
    public static Map<String, String> flatten(Config config) {
        Objects.requireNonNull(config, "Config must not be null");

        final Map<String, String> result = new LinkedHashMap<>();
        flattenObject(config.root(), "", result);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Recursively flattens a {@link ConfigObject} into the result map.
     */
    private static void flattenObject(ConfigObject obj, String prefix, Map<String, String> result) {
        for (Map.Entry<String, ConfigValue> entry : obj.entrySet()) {
            final String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            flattenValue(entry.getValue(), key, result);
        }
    }

    /**
     * Flattens a single {@link ConfigValue} into the result map based on its type.
     */
    private static void flattenValue(ConfigValue value, String key, Map<String, String> result) {
        if (value == null || value.valueType() == ConfigValueType.NULL) {
            return;
        }

        switch (value.valueType()) {
            case OBJECT:
                flattenObject((ConfigObject) value, key, result);
                break;
            case LIST:
                flattenList((ConfigList) value, key, result);
                break;
            case STRING:
            case NUMBER:
            case BOOLEAN:
                result.put(key, value.unwrapped().toString());
                break;
            default:
                // Unknown types are rendered via Config's own render mechanism
                result.put(key, value.render());
                break;
        }
    }

    /**
     * Flattens a {@link ConfigList} by indexing each element numerically.
     */
    private static void flattenList(ConfigList list, String prefix, Map<String, String> result) {
        for (int i = 0; i < list.size(); i++) {
            final String indexedKey = prefix + "." + i;
            flattenValue(list.get(i), indexedKey, result);
        }
    }
}
