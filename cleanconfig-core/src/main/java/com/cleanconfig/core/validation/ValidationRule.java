package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;

import java.util.function.Predicate;

/**
 * Functional interface for validating property values with context awareness.
 *
 * <p>Validation rules can be composed using {@link #and(ValidationRule)},
 * {@link #or(ValidationRule)}, and {@link #onlyIf(Predicate)} for building
 * complex validation logic from simple, reusable rules.
 *
 * <p>Example usage:
 * <pre>
 * // Simple rule
 * ValidationRule&lt;String&gt; notBlank = (name, value, context) -&gt; {
 *     if (value == null || value.trim().isEmpty()) {
 *         return ValidationResult.failure(
 *             ValidationError.builder()
 *                 .propertyName(name)
 *                 .errorMessage("Value cannot be blank")
 *                 .build()
 *         );
 *     }
 *     return ValidationResult.success();
 * };
 *
 * // Composed rule
 * ValidationRule&lt;String&gt; emailRule = notBlank
 *     .and(Rules.matchesRegex(EMAIL_PATTERN))
 *     .and(Rules.endsWith("@company.com"));
 * </pre>
 *
 * @param <T> the type of value to validate
 * @since 0.1.0
 */
@FunctionalInterface
public interface ValidationRule<T> {

    /**
     * Validates a property value.
     *
     * @param propertyName the name of the property being validated
     * @param value the value to validate (may be null)
     * @param context access to all properties and validation state
     * @return validation result indicating success or failure
     */
    ValidationResult validate(String propertyName, T value, PropertyContext context);

    /**
     * Combines this rule with another using AND logic.
     *
     * <p>The combined rule passes only if both rules pass. If this rule fails,
     * the other rule is not executed.
     *
     * @param other the other rule to combine with
     * @return a new rule that passes only if both rules pass
     */
    default ValidationRule<T> and(ValidationRule<T> other) {
        return (name, value, context) -> {
            ValidationResult first = this.validate(name, value, context);
            if (!first.isValid()) {
                return first;
            }
            return other.validate(name, value, context);
        };
    }

    /**
     * Combines this rule with another using OR logic.
     *
     * <p>The combined rule passes if either rule passes. If this rule passes,
     * the other rule is not executed.
     *
     * @param other the other rule to combine with
     * @return a new rule that passes if either rule passes
     */
    default ValidationRule<T> or(ValidationRule<T> other) {
        return (name, value, context) -> {
            ValidationResult first = this.validate(name, value, context);
            if (first.isValid()) {
                return first;
            }
            return other.validate(name, value, context);
        };
    }

    /**
     * Makes this rule conditional based on a predicate.
     *
     * <p>The rule is only executed if the condition is true. If the condition
     * is false, validation always succeeds.
     *
     * <p>Example usage:
     * <pre>
     * ValidationRule&lt;String&gt; sslRule = Rules.required()
     *     .onlyIf(ctx -&gt; "true".equals(ctx.getProperty("ssl.enabled").orElse("false")));
     * </pre>
     *
     * @param condition the condition that must be true for this rule to execute
     * @return a new conditional rule
     */
    default ValidationRule<T> onlyIf(Predicate<PropertyContext> condition) {
        return (name, value, context) -> {
            if (!condition.test(context)) {
                return ValidationResult.success();
            }
            return this.validate(name, value, context);
        };
    }

    /**
     * Creates a rule that always passes.
     *
     * @param <T> the value type
     * @return a rule that always succeeds
     */
    static <T> ValidationRule<T> alwaysValid() {
        return (name, value, context) -> ValidationResult.success();
    }

    /**
     * Creates a rule that always fails with the given message.
     *
     * @param errorMessage the error message
     * @param <T> the value type
     * @return a rule that always fails
     */
    static <T> ValidationRule<T> alwaysFails(String errorMessage) {
        return (name, value, context) -> ValidationResult.failure(
                ValidationError.builder()
                        .propertyName(name)
                        .errorMessage(errorMessage)
                        .build()
        );
    }
}
