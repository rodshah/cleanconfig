package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Validation rule that validates multiple properties together.
 *
 * <p>Multi-property validation rules are used when validation logic depends on
 * multiple property values simultaneously. Common use cases include:
 * <ul>
 *   <li>Numeric relationships (e.g., min &lt; max)</li>
 *   <li>Mutual exclusivity (e.g., only one of A or B can be set)</li>
 *   <li>At-least-one-required (e.g., at least one of A, B, or C must be set)</li>
 *   <li>Conditional requirements (e.g., if A is set, B must be set)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Validate that min &lt; max
 * MultiPropertyValidationRule rangeRule = (propertyNames, context) -&gt; {
 *     Integer min = context.getTypedProperty("range.min", Integer.class).orElse(0);
 *     Integer max = context.getTypedProperty("range.max", Integer.class).orElse(100);
 *
 *     if (min &gt;= max) {
 *         return ValidationResult.failure(
 *             ValidationError.builder()
 *                 .propertyName("range.max")
 *                 .errorMessage("Max must be greater than min")
 *                 .actualValue(String.valueOf(max))
 *                 .expectedValue("Value greater than " + min)
 *                 .build()
 *         );
 *     }
 *     return ValidationResult.success();
 * };
 * </pre>
 *
 * <p>Rules can be composed using {@link #and(MultiPropertyValidationRule)} and
 * {@link #or(MultiPropertyValidationRule)} for complex validation logic.
 *
 * @see MultiPropertyRules
 * @since 0.2.0
 */
@FunctionalInterface
public interface MultiPropertyValidationRule {

    /**
     * Validates multiple properties using the provided context.
     *
     * @param propertyNames the names of the properties being validated
     * @param context the property context providing access to all property values
     * @return the validation result
     * @throws NullPointerException if propertyNames or context is null
     */
    ValidationResult validate(String[] propertyNames, PropertyContext context);

    /**
     * Combines this rule with another using AND logic.
     * Both rules must pass for the combined rule to pass.
     *
     * @param other the other rule
     * @return a combined rule
     */
    default MultiPropertyValidationRule and(MultiPropertyValidationRule other) {
        Objects.requireNonNull(other, "Other rule cannot be null");
        return (propertyNames, context) -> {
            ValidationResult firstResult = this.validate(propertyNames, context);
            if (!firstResult.isValid()) {
                return firstResult;
            }
            return other.validate(propertyNames, context);
        };
    }

    /**
     * Combines this rule with another using OR logic.
     * At least one rule must pass for the combined rule to pass.
     *
     * @param other the other rule
     * @return a combined rule
     */
    default MultiPropertyValidationRule or(MultiPropertyValidationRule other) {
        Objects.requireNonNull(other, "Other rule cannot be null");
        return (propertyNames, context) -> {
            ValidationResult firstResult = this.validate(propertyNames, context);
            if (firstResult.isValid()) {
                return firstResult;
            }
            return other.validate(propertyNames, context);
        };
    }

    /**
     * Creates a conditional rule that only executes if the condition is true.
     *
     * @param condition the condition to check
     * @return a conditional rule
     */
    default MultiPropertyValidationRule onlyIf(Predicate<PropertyContext> condition) {
        Objects.requireNonNull(condition, "Condition cannot be null");
        return (propertyNames, context) -> {
            if (condition.test(context)) {
                return this.validate(propertyNames, context);
            }
            return ValidationResult.success();
        };
    }

    /**
     * Creates a rule that always passes validation.
     *
     * @return a rule that always returns success
     */
    static MultiPropertyValidationRule alwaysValid() {
        return (propertyNames, context) -> ValidationResult.success();
    }

    /**
     * Creates a rule that always fails validation with the given message.
     *
     * @param errorMessage the error message
     * @return a rule that always returns failure
     */
    static MultiPropertyValidationRule alwaysFails(String errorMessage) {
        Objects.requireNonNull(errorMessage, "Error message cannot be null");
        return (propertyNames, context) -> ValidationResult.failure(
                ValidationError.builder()
                        .propertyName(propertyNames.length > 0 ? propertyNames[0] : "unknown")
                        .errorMessage(errorMessage)
                        .build()
        );
    }
}
