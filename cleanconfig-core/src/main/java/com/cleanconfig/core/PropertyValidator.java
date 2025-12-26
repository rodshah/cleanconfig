package com.cleanconfig.core;

import com.cleanconfig.core.validation.ValidationResult;

import java.util.Map;

/**
 * Validates properties according to their definitions in a registry.
 *
 * <p>The property validator performs the following:
 * <ul>
 *   <li>Validates individual property values against their validation rules</li>
 *   <li>Applies type conversion before validation</li>
 *   <li>Respects validation order based on property dependencies</li>
 *   <li>Provides context for cross-property validation</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * PropertyValidator validator = new DefaultPropertyValidator(registry);
 * ValidationResult result = validator.validate(userProperties);
 *
 * if (!result.isValid()) {
 *     result.getErrors().forEach(error ->
 *         System.err.println(error.getPropertyName() + ": " + error.getErrorMessage())
 *     );
 * }
 * </pre>
 *
 * @since 0.1.0
 */
public interface PropertyValidator {

    /**
     * Validates all properties in the given map.
     *
     * @param properties the properties to validate
     * @return the validation result
     */
    ValidationResult validate(Map<String, String> properties);

    /**
     * Validates a single property value.
     *
     * @param propertyName the property name
     * @param value the property value
     * @param properties all properties (for cross-property validation)
     * @return the validation result
     */
    ValidationResult validateProperty(String propertyName, String value, Map<String, String> properties);

    /**
     * Validates properties in a specific context.
     *
     * @param properties the properties to validate
     * @param contextType the validation context type
     * @return the validation result
     */
    ValidationResult validate(Map<String, String> properties, ValidationContextType contextType);
}
