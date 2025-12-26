package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * General-purpose validation rules.
 *
 * <p>Provides rules for common validation patterns including null checks,
 * choice validation, and custom predicates.
 *
 * <p>Example usage:
 * <pre>
 * ValidationRule&lt;String&gt; environmentRule = GeneralRules.oneOf("dev", "staging", "prod");
 * ValidationRule&lt;Integer&gt; requiredPort = GeneralRules.required();
 * ValidationRule&lt;String&gt; customRule = GeneralRules.custom(
 *     value -&gt; value.length() &gt; 5,
 *     "Value must have more than 5 characters"
 * );
 * </pre>
 *
 * @since 0.1.0
 */
public final class GeneralRules {

    private GeneralRules() {
        // Utility class
    }

    /**
     * Validates that a value is required (not null).
     *
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> required() {
        return (name, value, context) -> {
            if (value == null) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value is required")
                                .actualValue("null")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is not null.
     * Alias for {@link #required()}.
     *
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> notNull() {
        return required();
    }

    /**
     * Validates that a value is one of the allowed values.
     *
     * @param allowedValues collection of allowed values
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> oneOf(Collection<T> allowedValues) {
        Set<T> allowed = new HashSet<>(allowedValues);
        return (name, value, context) -> {
            if (value != null && !allowed.contains(value)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be one of: " + allowedValues)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is one of the allowed values (varargs version).
     *
     * @param allowedValues allowed values
     * @param <T> the value type
     * @return validation rule
     */
    @SafeVarargs
    public static <T> ValidationRule<T> oneOf(T... allowedValues) {
        return oneOf(Arrays.asList(allowedValues));
    }

    /**
     * Validates that a value is not one of the forbidden values.
     *
     * @param forbiddenValues collection of forbidden values
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> noneOf(Collection<T> forbiddenValues) {
        Set<T> forbidden = new HashSet<>(forbiddenValues);
        return (name, value, context) -> {
            if (value != null && forbidden.contains(value)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must not be one of: " + forbiddenValues)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is not one of the forbidden values (varargs version).
     *
     * @param forbiddenValues forbidden values
     * @param <T> the value type
     * @return validation rule
     */
    @SafeVarargs
    public static <T> ValidationRule<T> noneOf(T... forbiddenValues) {
        return noneOf(Arrays.asList(forbiddenValues));
    }

    /**
     * Validates that a value equals an expected value.
     *
     * @param expectedValue the expected value
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> equalTo(T expectedValue) {
        return (name, value, context) -> {
            if (value == null && expectedValue != null) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must equal: " + expectedValue)
                                .actualValue("null")
                                .expectedValue(String.valueOf(expectedValue))
                                .build()
                );
            }
            if (value != null && !value.equals(expectedValue)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must equal: " + expectedValue)
                                .actualValue(String.valueOf(value))
                                .expectedValue(String.valueOf(expectedValue))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value does not equal a forbidden value.
     *
     * @param forbiddenValue the forbidden value
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> notEqualTo(T forbiddenValue) {
        return (name, value, context) -> {
            if (value != null && value.equals(forbiddenValue)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must not equal: " + forbiddenValue)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Creates a custom validation rule using a predicate.
     *
     * @param predicate the validation predicate (returns true if valid)
     * @param errorMessage the error message if validation fails
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> custom(Predicate<T> predicate, String errorMessage) {
        return (name, value, context) -> {
            if (value != null && !predicate.test(value)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage(errorMessage)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Creates a custom validation rule using a predicate with custom error details.
     *
     * @param predicate the validation predicate (returns true if valid)
     * @param errorMessage the error message if validation fails
     * @param expectedValue description of expected value
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> custom(Predicate<T> predicate, String errorMessage, String expectedValue) {
        return (name, value, context) -> {
            if (value != null && !predicate.test(value)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage(errorMessage)
                                .actualValue(String.valueOf(value))
                                .expectedValue(expectedValue)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value satisfies a custom predicate with access to context.
     *
     * <p>This allows complex validation logic that depends on other properties.
     *
     * @param contextPredicate the validation predicate with context access
     * @param errorMessage the error message if validation fails
     * @param <T> the value type
     * @return validation rule
     */
    public static <T> ValidationRule<T> customWithContext(
            java.util.function.BiPredicate<T, com.cleanconfig.core.PropertyContext> contextPredicate,
            String errorMessage) {
        return (name, value, context) -> {
            if (value != null && !contextPredicate.test(value, context)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage(errorMessage)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a Comparable value is within a range.
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @param <T> the comparable type
     * @return validation rule
     */
    public static <T extends Comparable<T>> ValidationRule<T> comparableBetween(T min, T max) {
        return (name, value, context) -> {
            if (value != null) {
                if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Value must be between " + min + " and " + max)
                                    .actualValue(String.valueOf(value))
                                    .expectedValue("[" + min + ", " + max + "]")
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a Comparable value is greater than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the comparable type
     * @return validation rule
     */
    public static <T extends Comparable<T>> ValidationRule<T> comparableGreaterThan(T threshold) {
        return (name, value, context) -> {
            if (value != null && value.compareTo(threshold) <= 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be greater than " + threshold)
                                .actualValue(String.valueOf(value))
                                .expectedValue("> " + threshold)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a Comparable value is less than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the comparable type
     * @return validation rule
     */
    public static <T extends Comparable<T>> ValidationRule<T> comparableLessThan(T threshold) {
        return (name, value, context) -> {
            if (value != null && value.compareTo(threshold) >= 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be less than " + threshold)
                                .actualValue(String.valueOf(value))
                                .expectedValue("< " + threshold)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }
}
