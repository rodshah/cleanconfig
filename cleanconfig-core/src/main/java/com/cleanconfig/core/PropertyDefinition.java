package com.cleanconfig.core;

import com.cleanconfig.core.validation.ValidationRule;

import java.util.Optional;
import java.util.Set;

/**
 * Metadata about a property including its type, validation, and defaults.
 *
 * <p>A property definition declares:
 * <ul>
 *   <li>Property name and description</li>
 *   <li>Value type for type-safe access</li>
 *   <li>Validation rules</li>
 *   <li>Default value (static or conditional)</li>
 *   <li>Required/optional status</li>
 *   <li>Category for organization</li>
 *   <li>Dependencies on other properties</li>
 *   <li>Validation order for dependency-aware validation</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * PropertyDefinition&lt;Integer&gt; SERVER_PORT = PropertyDefinition.builder(Integer.class)
 *     .name("server.port")
 *     .description("HTTP server port")
 *     .defaultValue(8080)
 *     .validationRule(Rules.integerBetween(1024, 65535))
 *     .category(PropertyCategory.NETWORKING)
 *     .required(true)
 *     .build();
 * </pre>
 *
 * @param <T> the type of the property value
 * @since 0.1.0
 */
public interface PropertyDefinition<T> {

    /**
     * Gets the property name.
     *
     * @return the property name (e.g., "server.port")
     */
    String getName();

    /**
     * Gets the property description.
     *
     * @return optional containing the description, or empty if not set
     */
    Optional<String> getDescription();

    /**
     * Gets the property value type.
     *
     * @return the value type class
     */
    Class<T> getType();

    /**
     * Gets the validation rule for this property.
     *
     * @return optional containing the validation rule, or empty if no validation
     */
    Optional<ValidationRule<T>> getValidationRule();

    /**
     * Gets the default value provider.
     *
     * @return optional containing the default value provider, or empty if no default
     */
    Optional<ConditionalDefaultValue<T>> getDefaultValue();

    /**
     * Checks if this property is required.
     *
     * <p>Required properties must have a value (either provided or defaulted).
     *
     * @return true if required, false if optional
     */
    boolean isRequired();

    /**
     * Gets the property category.
     *
     * @return the property category
     */
    PropertyCategory getCategory();

    /**
     * Gets the set of property names this property depends on for validation.
     *
     * <p>Dependencies ensure validation happens in the correct order.
     *
     * @return immutable set of dependency property names
     */
    Set<String> getDependsOnForValidation();

    /**
     * Gets the validation order hint.
     *
     * <p>Lower numbers are validated first. Properties with the same order
     * are validated in dependency-aware order if they have dependencies,
     * otherwise in arbitrary order.
     *
     * @return validation order (default: 0)
     */
    int getValidationOrder();

    /**
     * Checks if this property is deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    boolean isDeprecated();

    /**
     * Gets the deprecation message if this property is deprecated.
     *
     * @return optional containing the deprecation message, or empty if not deprecated
     */
    Optional<String> getDeprecationMessage();

    /**
     * Gets the suggested replacement property name if this property is deprecated.
     *
     * @return optional containing the replacement property name, or empty if none
     */
    Optional<String> getReplacementProperty();
}
