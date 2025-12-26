package com.cleanconfig.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Result of applying default values to properties.
 *
 * <p>This immutable class contains:
 * <ul>
 *   <li>The final properties map (user values + applied defaults)</li>
 *   <li>Information about which defaults were applied</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class DefaultApplicationResult {

    private final Map<String, String> propertiesWithDefaults;
    private final DefaultApplicationInfo applicationInfo;

    /**
     * Creates a new default application result.
     *
     * @param propertiesWithDefaults the properties with defaults applied
     * @param applicationInfo information about applied defaults
     */
    public DefaultApplicationResult(
            Map<String, String> propertiesWithDefaults,
            DefaultApplicationInfo applicationInfo) {
        this.propertiesWithDefaults = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(propertiesWithDefaults, "Properties cannot be null")));
        this.applicationInfo = Objects.requireNonNull(applicationInfo, "Application info cannot be null");
    }

    /**
     * Gets the properties with defaults applied.
     *
     * @return immutable map of properties with defaults
     */
    public Map<String, String> getPropertiesWithDefaults() {
        return propertiesWithDefaults;
    }

    /**
     * Gets information about applied defaults.
     *
     * @return the application info
     */
    public DefaultApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    @Override
    public String toString() {
        return "DefaultApplicationResult{"
                + "propertyCount=" + propertiesWithDefaults.size()
                + ", defaultsApplied=" + applicationInfo.getAppliedDefaultsCount()
                + '}';
    }
}
