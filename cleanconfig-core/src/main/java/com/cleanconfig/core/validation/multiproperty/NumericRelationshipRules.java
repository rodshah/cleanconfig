package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.validation.MultiPropertyValidationRule;
import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.Objects;
import java.util.Optional;

/**
 * Factory for creating numeric relationship validation rules.
 *
 * <p>Provides validation rules for comparing numeric properties:
 * <ul>
 *   <li>{@link #lessThan(String, String, Class)} - first &lt; second</li>
 *   <li>{@link #lessThanOrEqual(String, String, Class)} - first &lt;= second</li>
 *   <li>{@link #greaterThan(String, String, Class)} - first &gt; second</li>
 *   <li>{@link #greaterThanOrEqual(String, String, Class)} - first &gt;= second</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Validate min &lt; max
 * MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
 *     "range.min", "range.max", Integer.class
 * );
 *
 * // Validate start date before end date
 * MultiPropertyValidationRule dateRule = NumericRelationshipRules.lessThan(
 *     "event.startDate", "event.endDate", LocalDate.class
 * );
 * </pre>
 *
 * @since 0.2.0
 */
public final class NumericRelationshipRules {

    private NumericRelationshipRules() {
        // Utility class
    }

    /**
     * Validates that the first property is less than the second.
     *
     * <p>If either property is missing, validation passes (optional properties).
     * If both are present, enforces first &lt; second.
     *
     * @param firstProperty the first property name
     * @param secondProperty the second property name
     * @param type the comparable type
     * @param <T> the type parameter
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends Comparable<T>> MultiPropertyValidationRule lessThan(
            String firstProperty,
            String secondProperty,
            Class<T> type) {
        Objects.requireNonNull(firstProperty, "First property name cannot be null");
        Objects.requireNonNull(secondProperty, "Second property name cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");

        return (propertyNames, context) -> {
            Optional<T> first = context.getTypedProperty(firstProperty, type);
            Optional<T> second = context.getTypedProperty(secondProperty, type);

            if (!first.isPresent() || !second.isPresent()) {
                return ValidationResult.success(); // Skip if either is missing
            }

            if (first.get().compareTo(second.get()) >= 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(firstProperty)
                                .actualValue(String.valueOf(first.get()))
                                .errorMessage(firstProperty + " must be less than " + secondProperty)
                                .expectedValue("Value less than " + second.get())
                                .build()
                );
            }

            return ValidationResult.success();
        };
    }

    /**
     * Validates that the first property is less than or equal to the second.
     *
     * <p>If either property is missing, validation passes (optional properties).
     * If both are present, enforces first &lt;= second.
     *
     * @param firstProperty the first property name
     * @param secondProperty the second property name
     * @param type the comparable type
     * @param <T> the type parameter
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends Comparable<T>> MultiPropertyValidationRule lessThanOrEqual(
            String firstProperty,
            String secondProperty,
            Class<T> type) {
        Objects.requireNonNull(firstProperty, "First property name cannot be null");
        Objects.requireNonNull(secondProperty, "Second property name cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");

        return (propertyNames, context) -> {
            Optional<T> first = context.getTypedProperty(firstProperty, type);
            Optional<T> second = context.getTypedProperty(secondProperty, type);

            if (!first.isPresent() || !second.isPresent()) {
                return ValidationResult.success(); // Skip if either is missing
            }

            if (first.get().compareTo(second.get()) > 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(firstProperty)
                                .actualValue(String.valueOf(first.get()))
                                .errorMessage(firstProperty + " must be less than or equal to " + secondProperty)
                                .expectedValue("Value <= " + second.get())
                                .build()
                );
            }

            return ValidationResult.success();
        };
    }

    /**
     * Validates that the first property is greater than the second.
     *
     * <p>If either property is missing, validation passes (optional properties).
     * If both are present, enforces first &gt; second.
     *
     * @param firstProperty the first property name
     * @param secondProperty the second property name
     * @param type the comparable type
     * @param <T> the type parameter
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends Comparable<T>> MultiPropertyValidationRule greaterThan(
            String firstProperty,
            String secondProperty,
            Class<T> type) {
        return lessThan(secondProperty, firstProperty, type);
    }

    /**
     * Validates that the first property is greater than or equal to the second.
     *
     * <p>If either property is missing, validation passes (optional properties).
     * If both are present, enforces first &gt;= second.
     *
     * @param firstProperty the first property name
     * @param secondProperty the second property name
     * @param type the comparable type
     * @param <T> the type parameter
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends Comparable<T>> MultiPropertyValidationRule greaterThanOrEqual(
            String firstProperty,
            String secondProperty,
            Class<T> type) {
        return lessThanOrEqual(secondProperty, firstProperty, type);
    }
}
