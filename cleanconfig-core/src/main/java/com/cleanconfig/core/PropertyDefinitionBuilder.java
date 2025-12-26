package com.cleanconfig.core;

import com.cleanconfig.core.validation.ValidationRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Builder for creating {@link PropertyDefinition} instances.
 *
 * <p>Example usage:
 * <pre>
 * PropertyDefinition&lt;Integer&gt; serverPort = PropertyDefinition.builder(Integer.class)
 *     .name("server.port")
 *     .description("HTTP server port")
 *     .defaultValue(8080)
 *     .validationRule(Rules.port())
 *     .required(true)
 *     .build();
 * </pre>
 *
 * @param <T> the property value type
 * @since 0.1.0
 */
public class PropertyDefinitionBuilder<T> {

    private final Class<T> type;
    private String name;
    private String description;
    private ValidationRule<T> validationRule;
    private ConditionalDefaultValue<T> defaultValue;
    private boolean required = false;
    private PropertyCategory category = PropertyCategory.GENERAL;
    private Set<String> dependsOnForValidation = new HashSet<>();
    private int validationOrder = 0;
    private boolean deprecated = false;
    private String deprecationMessage;
    private String replacementProperty;

    /**
     * Creates a new builder for the specified type.
     *
     * @param type the property value type
     */
    public PropertyDefinitionBuilder(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
    }

    /**
     * Sets the property name.
     *
     * @param name the property name
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the property description.
     *
     * @param description the description
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the validation rule.
     *
     * @param validationRule the validation rule
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> validationRule(ValidationRule<T> validationRule) {
        this.validationRule = validationRule;
        return this;
    }

    /**
     * Sets a static default value.
     *
     * @param defaultValue the default value
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = ConditionalDefaultValue.staticValue(defaultValue);
        return this;
    }

    /**
     * Sets a conditional default value provider.
     *
     * @param defaultValue the default value provider
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> defaultValue(ConditionalDefaultValue<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets whether this property is required.
     *
     * @param required true if required, false if optional
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> required(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Sets the property category.
     *
     * @param category the category
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> category(PropertyCategory category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the properties this property depends on for validation.
     *
     * @param dependsOn the property names this property depends on
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> dependsOnForValidation(String... dependsOn) {
        this.dependsOnForValidation = new HashSet<>(Arrays.asList(dependsOn));
        return this;
    }

    /**
     * Sets the validation order.
     *
     * @param validationOrder the validation order (lower numbers validated first)
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> validationOrder(int validationOrder) {
        this.validationOrder = validationOrder;
        return this;
    }

    /**
     * Marks this property as deprecated.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> deprecated() {
        this.deprecated = true;
        return this;
    }

    /**
     * Marks this property as deprecated with a message.
     *
     * @param message the deprecation message
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> deprecated(String message) {
        this.deprecated = true;
        this.deprecationMessage = message;
        return this;
    }

    /**
     * Sets the replacement property for a deprecated property.
     *
     * @param replacementProperty the replacement property name
     * @return this builder
     */
    public PropertyDefinitionBuilder<T> replacedBy(String replacementProperty) {
        this.replacementProperty = replacementProperty;
        return this;
    }

    /**
     * Builds the property definition.
     *
     * @return the property definition
     * @throws IllegalStateException if name is not set
     */
    public PropertyDefinition<T> build() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Property name is required");
        }
        return new DefaultPropertyDefinition<>(this);
    }

    /**
     * Default implementation of PropertyDefinition.
     */
    private static class DefaultPropertyDefinition<T> implements PropertyDefinition<T> {
        private final String name;
        private final String description;
        private final Class<T> type;
        private final ValidationRule<T> validationRule;
        private final ConditionalDefaultValue<T> defaultValue;
        private final boolean required;
        private final PropertyCategory category;
        private final Set<String> dependsOnForValidation;
        private final int validationOrder;
        private final boolean deprecated;
        private final String deprecationMessage;
        private final String replacementProperty;

        DefaultPropertyDefinition(PropertyDefinitionBuilder<T> builder) {
            this.name = builder.name;
            this.description = builder.description;
            this.type = builder.type;
            this.validationRule = builder.validationRule;
            this.defaultValue = builder.defaultValue;
            this.required = builder.required;
            this.category = builder.category;
            this.dependsOnForValidation = Collections.unmodifiableSet(
                    new HashSet<>(builder.dependsOnForValidation));
            this.validationOrder = builder.validationOrder;
            this.deprecated = builder.deprecated;
            this.deprecationMessage = builder.deprecationMessage;
            this.replacementProperty = builder.replacementProperty;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Class<T> getType() {
            return type;
        }

        @Override
        public Optional<ValidationRule<T>> getValidationRule() {
            return Optional.ofNullable(validationRule);
        }

        @Override
        public Optional<ConditionalDefaultValue<T>> getDefaultValue() {
            return Optional.ofNullable(defaultValue);
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public PropertyCategory getCategory() {
            return category;
        }

        @Override
        public Set<String> getDependsOnForValidation() {
            return dependsOnForValidation;
        }

        @Override
        public int getValidationOrder() {
            return validationOrder;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public Optional<String> getDeprecationMessage() {
            return Optional.ofNullable(deprecationMessage);
        }

        @Override
        public Optional<String> getReplacementProperty() {
            return Optional.ofNullable(replacementProperty);
        }

        @Override
        public String toString() {
            return "PropertyDefinition{name='" + name + "', type=" + type.getSimpleName() + ", required=" + required + "}";
        }
    }

    /**
     * Static factory method for creating a builder.
     *
     * @param type the property value type
     * @param <T> the property value type
     * @return a new builder
     */
    public static <T> PropertyDefinitionBuilder<T> builder(Class<T> type) {
        return new PropertyDefinitionBuilder<>(type);
    }
}
