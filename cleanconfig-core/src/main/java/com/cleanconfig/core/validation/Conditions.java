package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Factory for creating conditional predicates used with {@link ValidationRule#onlyIf(Predicate)}.
 *
 * <p>Conditions allow validation rules to be executed conditionally based on:
 * <ul>
 *   <li>Other property values</li>
 *   <li>Metadata values</li>
 *   <li>Custom predicates</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Only validate SSL cert path if SSL is enabled
 * ValidationRule&lt;String&gt; sslCertRule = Rules.fileExists()
 *     .onlyIf(Conditions.propertyEquals("ssl.enabled", "true"));
 *
 * // Only require password in production
 * ValidationRule&lt;String&gt; passwordRule = Rules.required()
 *     .onlyIf(Conditions.propertyEquals("environment", "production"));
 *
 * // Complex condition using AND/OR
 * ValidationRule&lt;Integer&gt; timeoutRule = Rules.positive()
 *     .onlyIf(
 *         Conditions.propertyEquals("mode", "async")
 *             .and(Conditions.propertyIsTrue("timeout.enabled"))
 *     );
 * </pre>
 *
 * @since 0.1.0
 */
public final class Conditions {

    private Conditions() {
        // Utility class
    }

    /**
     * Creates a condition that checks if a property equals a specific value.
     *
     * @param propertyName the property name
     * @param expectedValue the expected value
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyEquals(String propertyName, String expectedValue) {
        return context -> context.getProperty(propertyName)
                .map(value -> value.equals(expectedValue))
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property does not equal a specific value.
     *
     * @param propertyName the property name
     * @param forbiddenValue the forbidden value
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyNotEquals(String propertyName, String forbiddenValue) {
        return context -> context.getProperty(propertyName)
                .map(value -> !value.equals(forbiddenValue))
                .orElse(true);
    }

    /**
     * Creates a condition that checks if a property is present (has any value).
     *
     * @param propertyName the property name
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyIsPresent(String propertyName) {
        return context -> context.hasProperty(propertyName);
    }

    /**
     * Creates a condition that checks if a property is absent.
     *
     * @param propertyName the property name
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyIsAbsent(String propertyName) {
        return context -> !context.hasProperty(propertyName);
    }

    /**
     * Creates a condition that checks if a property is true.
     * Accepts: "true", "yes", "1" (case-insensitive).
     *
     * @param propertyName the property name
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyIsTrue(String propertyName) {
        return context -> context.getProperty(propertyName)
                .map(value -> {
                    String v = value.trim().toLowerCase();
                    return "true".equals(v) || "yes".equals(v) || "1".equals(v);
                })
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property is false.
     * Accepts: "false", "no", "0" (case-insensitive).
     *
     * @param propertyName the property name
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyIsFalse(String propertyName) {
        return context -> context.getProperty(propertyName)
                .map(value -> {
                    String v = value.trim().toLowerCase();
                    return "false".equals(v) || "no".equals(v) || "0".equals(v);
                })
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property matches a predicate.
     *
     * @param propertyName the property name
     * @param predicate the predicate to test the property value
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyMatches(String propertyName, Predicate<String> predicate) {
        return context -> context.getProperty(propertyName)
                .map(predicate::test)
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a typed property matches a predicate.
     *
     * @param propertyName the property name
     * @param type the target type
     * @param predicate the predicate to test the converted value
     * @param <T> the property type
     * @return condition predicate
     */
    public static <T> Predicate<PropertyContext> typedPropertyMatches(
            String propertyName,
            Class<T> type,
            Predicate<T> predicate) {
        return context -> context.getTypedProperty(propertyName, type)
                .map(predicate::test)
                .orElse(false);
    }

    /**
     * Creates a condition that checks if metadata equals a specific value.
     *
     * @param metadataKey the metadata key
     * @param expectedValue the expected value
     * @return condition predicate
     */
    public static Predicate<PropertyContext> metadataEquals(String metadataKey, String expectedValue) {
        return context -> context.getMetadata(metadataKey)
                .map(value -> value.equals(expectedValue))
                .orElse(false);
    }

    /**
     * Creates a condition that checks if metadata is present.
     *
     * @param metadataKey the metadata key
     * @return condition predicate
     */
    public static Predicate<PropertyContext> metadataIsPresent(String metadataKey) {
        return context -> context.getMetadata(metadataKey).isPresent();
    }

    /**
     * Creates a condition that always evaluates to true.
     *
     * @return condition predicate that always returns true
     */
    public static Predicate<PropertyContext> alwaysTrue() {
        return context -> true;
    }

    /**
     * Creates a condition that always evaluates to false.
     *
     * @return condition predicate that always returns false
     */
    public static Predicate<PropertyContext> alwaysFalse() {
        return context -> false;
    }

    /**
     * Creates a condition that negates another condition.
     *
     * @param condition the condition to negate
     * @return negated condition predicate
     */
    public static Predicate<PropertyContext> not(Predicate<PropertyContext> condition) {
        return condition.negate();
    }

    /**
     * Creates a condition that combines multiple conditions with AND logic.
     * All conditions must be true for the result to be true.
     *
     * @param conditions the conditions to combine
     * @return combined condition predicate
     */
    @SafeVarargs
    public static Predicate<PropertyContext> and(Predicate<PropertyContext>... conditions) {
        return context -> {
            for (Predicate<PropertyContext> condition : conditions) {
                if (!condition.test(context)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Creates a condition that combines multiple conditions with OR logic.
     * At least one condition must be true for the result to be true.
     *
     * @param conditions the conditions to combine
     * @return combined condition predicate
     */
    @SafeVarargs
    public static Predicate<PropertyContext> or(Predicate<PropertyContext>... conditions) {
        return context -> {
            for (Predicate<PropertyContext> condition : conditions) {
                if (condition.test(context)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Creates a condition that checks if all properties are present.
     *
     * @param propertyNames the property names
     * @return condition predicate
     */
    public static Predicate<PropertyContext> allPropertiesPresent(String... propertyNames) {
        return context -> {
            for (String propertyName : propertyNames) {
                if (!context.hasProperty(propertyName)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Creates a condition that checks if any property is present.
     *
     * @param propertyNames the property names
     * @return condition predicate
     */
    public static Predicate<PropertyContext> anyPropertyPresent(String... propertyNames) {
        return context -> {
            for (String propertyName : propertyNames) {
                if (context.hasProperty(propertyName)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Creates a condition that checks if a property's integer value is within a range.
     *
     * @param propertyName the property name
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return condition predicate
     */
    public static Predicate<PropertyContext> integerPropertyBetween(String propertyName, int min, int max) {
        return context -> {
            Optional<Integer> value = context.getTypedProperty(propertyName, Integer.class);
            return value.map(v -> v >= min && v <= max).orElse(false);
        };
    }

    /**
     * Creates a condition that checks if a property's value is in a set of allowed values.
     *
     * @param propertyName the property name
     * @param allowedValues the allowed values
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyOneOf(String propertyName, String... allowedValues) {
        return context -> context.getProperty(propertyName)
                .map(value -> {
                    for (String allowed : allowedValues) {
                        if (value.equals(allowed)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property starts with a prefix.
     *
     * @param propertyName the property name
     * @param prefix the required prefix
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyStartsWith(String propertyName, String prefix) {
        return context -> context.getProperty(propertyName)
                .map(value -> value.startsWith(prefix))
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property ends with a suffix.
     *
     * @param propertyName the property name
     * @param suffix the required suffix
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyEndsWith(String propertyName, String suffix) {
        return context -> context.getProperty(propertyName)
                .map(value -> value.endsWith(suffix))
                .orElse(false);
    }

    /**
     * Creates a condition that checks if a property contains a substring.
     *
     * @param propertyName the property name
     * @param substring the required substring
     * @return condition predicate
     */
    public static Predicate<PropertyContext> propertyContains(String propertyName, String substring) {
        return context -> context.getProperty(propertyName)
                .map(value -> value.contains(substring))
                .orElse(false);
    }
}
