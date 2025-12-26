package com.cleanconfig.core;

import java.util.Map;

/**
 * Applies default values to properties that don't have user-provided values.
 *
 * <p>The applier is responsible for:
 * <ul>
 *   <li>Applying defaults only when no user value exists</li>
 *   <li>Handling static, conditional, and computed defaults</li>
 *   <li>Tracking which defaults were applied</li>
 *   <li>Operating as a pure function (no side effects)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * DefaultValueApplier applier = new DefaultValueApplierImpl(registry);
 * Map&lt;String, String&gt; userProperties = ...;
 * DefaultApplicationResult result = applier.applyDefaults(userProperties);
 *
 * // Get properties with defaults applied
 * Map&lt;String, String&gt; finalProperties = result.getPropertiesWithDefaults();
 *
 * // Check which defaults were applied
 * DefaultApplicationInfo info = result.getApplicationInfo();
 * if (info.wasDefaultApplied("server.port")) {
 *     System.out.println("Used default port: " + info.getAppliedValue("server.port"));
 * }
 * </pre>
 *
 * @since 0.1.0
 */
public interface DefaultValueApplier {

    /**
     * Applies default values to properties.
     *
     * <p>This method:
     * <ul>
     *   <li>Never overrides user-provided values</li>
     *   <li>Applies defaults only for missing properties</li>
     *   <li>Evaluates conditional and computed defaults</li>
     *   <li>Returns a new map (original is unchanged)</li>
     * </ul>
     *
     * @param userProperties the user-provided properties
     * @return result containing properties with defaults and application info
     */
    DefaultApplicationResult applyDefaults(Map<String, String> userProperties);
}
