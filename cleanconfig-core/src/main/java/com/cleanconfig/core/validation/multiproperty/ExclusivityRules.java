package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.validation.MultiPropertyValidationRule;
import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for creating exclusivity validation rules.
 *
 * <p>Provides validation rules for property exclusivity constraints:
 * <ul>
 *   <li>{@link #mutuallyExclusive(String...)} - at most one can be set</li>
 *   <li>{@link #atLeastOneRequired(String...)} - at least one must be set</li>
 *   <li>{@link #exactlyOneRequired(String...)} - exactly one must be set</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Only one authentication method allowed
 * MultiPropertyValidationRule authRule = ExclusivityRules.mutuallyExclusive(
 *     "auth.password", "auth.apiKey", "auth.certificate"
 * );
 *
 * // At least one contact method required
 * MultiPropertyValidationRule contactRule = ExclusivityRules.atLeastOneRequired(
 *     "contact.email", "contact.phone", "contact.address"
 * );
 *
 * // Exactly one deployment strategy
 * MultiPropertyValidationRule deployRule = ExclusivityRules.exactlyOneRequired(
 *     "deploy.rolling", "deploy.blueGreen", "deploy.canary"
 * );
 * </pre>
 *
 * @since 0.2.0
 */
public final class ExclusivityRules {

    private ExclusivityRules() {
        // Utility class
    }

    /**
     * Validates that at most one of the given properties is set.
     *
     * <p>All properties can be absent, but only one can be present at a time.
     * This is useful for mutually exclusive configuration options.
     *
     * @param propertyNames the property names (must be at least 2)
     * @return validation rule
     * @throws IllegalArgumentException if less than 2 properties provided
     * @throws NullPointerException if propertyNames is null
     */
    public static MultiPropertyValidationRule mutuallyExclusive(String... propertyNames) {
        Objects.requireNonNull(propertyNames, "Property names cannot be null");
        if (propertyNames.length < 2) {
            throw new IllegalArgumentException("At least 2 properties are required for mutual exclusivity");
        }

        return (names, context) -> {
            List<String> presentProperties = new ArrayList<>();

            for (String propertyName : propertyNames) {
                if (context.hasProperty(propertyName)) {
                    String value = context.getProperty(propertyName).orElse("");
                    if (!value.trim().isEmpty()) {
                        presentProperties.add(propertyName);
                    }
                }
            }

            if (presentProperties.size() > 1) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(presentProperties.get(0))
                                .errorMessage("Only one of [" + String.join(", ", propertyNames)
                                        + "] can be set, but found: " + String.join(", ", presentProperties))
                                .build()
                );
            }

            return ValidationResult.success();
        };
    }

    /**
     * Validates that at least one of the given properties is set.
     *
     * <p>At least one property must have a non-empty value. This is useful
     * when multiple options are available but at least one is required.
     *
     * @param propertyNames the property names (must be at least 1)
     * @return validation rule
     * @throws IllegalArgumentException if no properties provided
     * @throws NullPointerException if propertyNames is null
     */
    public static MultiPropertyValidationRule atLeastOneRequired(String... propertyNames) {
        Objects.requireNonNull(propertyNames, "Property names cannot be null");
        if (propertyNames.length < 1) {
            throw new IllegalArgumentException("At least 1 property is required");
        }

        return (names, context) -> {
            for (String propertyName : propertyNames) {
                if (context.hasProperty(propertyName)) {
                    String value = context.getProperty(propertyName).orElse("");
                    if (!value.trim().isEmpty()) {
                        return ValidationResult.success();
                    }
                }
            }

            return ValidationResult.failure(
                    ValidationError.builder()
                            .propertyName(propertyNames[0])
                            .errorMessage("At least one of [" + String.join(", ", propertyNames)
                                    + "] must be set")
                            .build()
            );
        };
    }

    /**
     * Validates that exactly one of the given properties is set.
     *
     * <p>Combines {@link #atLeastOneRequired(String...)} and {@link #mutuallyExclusive(String...)}.
     * Exactly one property must have a non-empty value - no more, no less.
     *
     * @param propertyNames the property names (must be at least 2)
     * @return validation rule
     * @throws IllegalArgumentException if less than 2 properties provided
     * @throws NullPointerException if propertyNames is null
     */
    public static MultiPropertyValidationRule exactlyOneRequired(String... propertyNames) {
        Objects.requireNonNull(propertyNames, "Property names cannot be null");
        if (propertyNames.length < 2) {
            throw new IllegalArgumentException("At least 2 properties are required");
        }

        return atLeastOneRequired(propertyNames).and(mutuallyExclusive(propertyNames));
    }
}
