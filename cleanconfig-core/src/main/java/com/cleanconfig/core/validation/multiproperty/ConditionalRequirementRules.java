package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.validation.MultiPropertyValidationRule;
import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for creating conditional requirement validation rules.
 *
 * <p>Provides validation rules for conditional dependencies between properties:
 * <ul>
 *   <li>{@link #ifThen(String, String)} - if A is set, then B must be set</li>
 *   <li>{@link #allOrNothing(String...)} - all properties must be set together, or none at all</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // If SSL is enabled, certificate path is required
 * MultiPropertyValidationRule sslRule = ConditionalRequirementRules.ifThen(
 *     "ssl.enabled", "ssl.certPath"
 * );
 *
 * // Database credentials must be set together
 * MultiPropertyValidationRule dbRule = ConditionalRequirementRules.allOrNothing(
 *     "db.username", "db.password", "db.host"
 * );
 * </pre>
 *
 * @since 0.2.0
 */
public final class ConditionalRequirementRules {

    private ConditionalRequirementRules() {
        // Utility class
    }

    /**
     * Validates that if the first property is set, the second must also be set.
     *
     * <p>Implements "if A then B" logic:
     * <ul>
     *   <li>If A is not set → passes (no requirement triggered)</li>
     *   <li>If A is set and B is set → passes</li>
     *   <li>If A is set and B is not set → fails</li>
     * </ul>
     *
     * @param ifProperty the property that triggers the requirement
     * @param thenProperty the property that must be set
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static MultiPropertyValidationRule ifThen(String ifProperty, String thenProperty) {
        Objects.requireNonNull(ifProperty, "If property name cannot be null");
        Objects.requireNonNull(thenProperty, "Then property name cannot be null");

        return (propertyNames, context) -> {
            // If condition not met, validation passes
            if (!context.hasProperty(ifProperty)) {
                return ValidationResult.success();
            }

            String ifValue = context.getProperty(ifProperty).orElse("");
            if (ifValue.trim().isEmpty()) {
                return ValidationResult.success();
            }

            // Condition met - check that "then" property is set
            if (!context.hasProperty(thenProperty)
                    || context.getProperty(thenProperty).orElse("").trim().isEmpty()) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(thenProperty)
                                .errorMessage("Property " + thenProperty + " is required when "
                                        + ifProperty + " is set")
                                .build()
                );
            }

            return ValidationResult.success();
        };
    }

    /**
     * Validates that if any of the given properties are set, all must be set.
     *
     * <p>Implements "all or nothing" logic:
     * <ul>
     *   <li>All properties set → passes</li>
     *   <li>No properties set → passes</li>
     *   <li>Some but not all set → fails</li>
     * </ul>
     *
     * <p>This is useful for property groups that form a logical unit
     * (e.g., database credentials, API authentication settings).
     *
     * @param propertyNames the property names (must be at least 2)
     * @return validation rule
     * @throws IllegalArgumentException if less than 2 properties provided
     * @throws NullPointerException if propertyNames is null
     */
    public static MultiPropertyValidationRule allOrNothing(String... propertyNames) {
        Objects.requireNonNull(propertyNames, "Property names cannot be null");
        if (propertyNames.length < 2) {
            throw new IllegalArgumentException("At least 2 properties are required");
        }

        return (names, context) -> {
            int presentCount = 0;
            List<String> missingProperties = new ArrayList<>();
            List<String> presentProperties = new ArrayList<>();

            for (String propertyName : propertyNames) {
                if (context.hasProperty(propertyName)
                        && !context.getProperty(propertyName).orElse("").trim().isEmpty()) {
                    presentCount++;
                    presentProperties.add(propertyName);
                } else {
                    missingProperties.add(propertyName);
                }
            }

            // All set or none set is valid
            if (presentCount == 0 || presentCount == propertyNames.length) {
                return ValidationResult.success();
            }

            // Some but not all are set - invalid
            return ValidationResult.failure(
                    ValidationError.builder()
                            .propertyName(missingProperties.get(0))
                            .errorMessage("All of [" + String.join(", ", propertyNames)
                                    + "] must be set together, or none at all. "
                                    + "Present: [" + String.join(", ", presentProperties) + "], "
                                    + "Missing: [" + String.join(", ", missingProperties) + "]")
                            .build()
            );
        };
    }
}
