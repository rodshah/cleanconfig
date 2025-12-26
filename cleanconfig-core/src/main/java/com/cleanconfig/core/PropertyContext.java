package com.cleanconfig.core;

import java.util.Map;
import java.util.Optional;

/**
 * Read-only context providing access to properties during validation.
 *
 * <p>The property context allows validation rules to:
 * <ul>
 *   <li>Access the value being validated</li>
 *   <li>Access other property values for cross-property validation</li>
 *   <li>Access metadata about the validation environment</li>
 * </ul>
 *
 * <p>Example usage in a validation rule:
 * <pre>
 * ValidationRule&lt;String&gt; cpuLimitRule = (name, value, context) -&gt; {
 *     Optional&lt;String&gt; request = context.getProperty("cpu.request");
 *     if (request.isPresent() &amp;&amp; value != null) {
 *         // Compare limit vs request
 *         if (parseCpu(value) &lt; parseCpu(request.get())) {
 *             return ValidationResult.failure(...);
 *         }
 *     }
 *     return ValidationResult.success();
 * };
 * </pre>
 *
 * @since 0.1.0
 */
public interface PropertyContext {

    /**
     * Gets a property value as a string.
     *
     * @param propertyName the property name
     * @return optional containing the value, or empty if not present
     */
    Optional<String> getProperty(String propertyName);

    /**
     * Gets a property value with type conversion.
     *
     * @param propertyName the property name
     * @param targetType the desired type
     * @param <T> the type parameter
     * @return optional containing the converted value, or empty if not present or conversion failed
     */
    <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType);

    /**
     * Gets all properties as an immutable map.
     *
     * @return immutable map of all properties
     */
    Map<String, String> getAllProperties();

    /**
     * Gets a metadata value.
     *
     * <p>Metadata can store additional information like environment, user, timestamp, etc.
     *
     * @param key the metadata key
     * @return optional containing the metadata value, or empty if not present
     */
    Optional<String> getMetadata(String key);

    /**
     * Checks if a property is present (regardless of value).
     *
     * @param propertyName the property name
     * @return true if the property is present
     */
    boolean hasProperty(String propertyName);
}
