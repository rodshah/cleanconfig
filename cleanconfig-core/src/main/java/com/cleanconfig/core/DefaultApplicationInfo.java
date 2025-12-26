package com.cleanconfig.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Tracks information about which default values were applied.
 *
 * <p>This immutable class records:
 * <ul>
 *   <li>Which properties had defaults applied</li>
 *   <li>The actual default values that were applied</li>
 *   <li>Total count of applied defaults</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * if (info.wasDefaultApplied("server.port")) {
 *     String defaultPort = info.getAppliedValue("server.port").orElse("unknown");
 *     System.out.println("Using default port: " + defaultPort);
 * }
 *
 * // Get all properties that used defaults
 * Set&lt;String&gt; propertiesWithDefaults = info.getPropertiesWithDefaults();
 * </pre>
 *
 * @since 0.1.0
 */
public class DefaultApplicationInfo {

    private final Map<String, String> appliedDefaults;

    /**
     * Creates a new default application info.
     *
     * @param appliedDefaults map of property names to their applied default values
     */
    public DefaultApplicationInfo(Map<String, String> appliedDefaults) {
        this.appliedDefaults = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(appliedDefaults, "Applied defaults cannot be null")));
    }

    /**
     * Checks if a default was applied for the specified property.
     *
     * @param propertyName the property name
     * @return true if a default was applied, false otherwise
     */
    public boolean wasDefaultApplied(String propertyName) {
        return appliedDefaults.containsKey(propertyName);
    }

    /**
     * Gets the default value that was applied for a property.
     *
     * @param propertyName the property name
     * @return optional containing the applied default value, or empty if no default was applied
     */
    public Optional<String> getAppliedValue(String propertyName) {
        return Optional.ofNullable(appliedDefaults.get(propertyName));
    }

    /**
     * Gets all property names that had defaults applied.
     *
     * @return immutable set of property names with applied defaults
     */
    public Set<String> getPropertiesWithDefaults() {
        return appliedDefaults.keySet();
    }

    /**
     * Gets the count of properties that had defaults applied.
     *
     * @return the number of applied defaults
     */
    public int getAppliedDefaultsCount() {
        return appliedDefaults.size();
    }

    /**
     * Gets all applied defaults as a map.
     *
     * @return immutable map of property names to their applied default values
     */
    public Map<String, String> getAllAppliedDefaults() {
        return appliedDefaults;
    }

    /**
     * Creates an empty application info (no defaults applied).
     *
     * @return empty application info
     */
    public static DefaultApplicationInfo empty() {
        return new DefaultApplicationInfo(Collections.emptyMap());
    }

    @Override
    public String toString() {
        return "DefaultApplicationInfo{"
                + "appliedDefaultsCount=" + appliedDefaults.size()
                + ", properties=" + appliedDefaults.keySet()
                + '}';
    }
}
