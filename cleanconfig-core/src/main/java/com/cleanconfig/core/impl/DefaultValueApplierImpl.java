package com.cleanconfig.core.impl;

import com.cleanconfig.core.ConditionalDefaultValue;
import com.cleanconfig.core.DefaultApplicationInfo;
import com.cleanconfig.core.DefaultApplicationResult;
import com.cleanconfig.core.DefaultValueApplier;
import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.converter.TypeConverterRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of {@link DefaultValueApplier}.
 *
 * <p>This implementation:
 * <ul>
 *   <li>Never overrides user-provided values</li>
 *   <li>Applies defaults in property definition order</li>
 *   <li>Evaluates conditional and computed defaults using property context</li>
 *   <li>Operates as a pure function (no side effects)</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class DefaultValueApplierImpl implements DefaultValueApplier {

    private final PropertyRegistry registry;
    private final TypeConverterRegistry converterRegistry;

    /**
     * Creates a new default value applier.
     *
     * @param registry the property registry
     */
    public DefaultValueApplierImpl(PropertyRegistry registry) {
        this(registry, TypeConverterRegistry.getInstance());
    }

    /**
     * Creates a new default value applier with a custom converter registry.
     *
     * @param registry the property registry
     * @param converterRegistry the type converter registry
     */
    public DefaultValueApplierImpl(PropertyRegistry registry, TypeConverterRegistry converterRegistry) {
        this.registry = Objects.requireNonNull(registry, "Property registry cannot be null");
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
    }

    @Override
    public DefaultApplicationResult applyDefaults(Map<String, String> userProperties) {
        return applyDefaults(userProperties, ValidationContextType.STARTUP);
    }

    @Override
    public DefaultApplicationResult applyDefaults(
            Map<String, String> userProperties,
            ValidationContextType contextType) {
        Objects.requireNonNull(userProperties, "User properties cannot be null");
        Objects.requireNonNull(contextType, "Context type cannot be null");

        // Create result map (copy of user properties + defaults)
        Map<String, String> result = new LinkedHashMap<>(userProperties);

        // Track applied defaults
        Map<String, String> appliedDefaults = new LinkedHashMap<>();

        // Apply defaults for all properties in the registry
        for (PropertyDefinition<?> definition : registry.getAllProperties()) {
            String propertyName = definition.getName();

            // Skip if user provided a value
            if (userProperties.containsKey(propertyName)) {
                continue;
            }

            // Try to apply default value
            Optional<String> defaultValue = evaluateDefault(definition, result, contextType);
            if (defaultValue.isPresent()) {
                result.put(propertyName, defaultValue.get());
                appliedDefaults.put(propertyName, defaultValue.get());
            }
        }

        return new DefaultApplicationResult(result, new DefaultApplicationInfo(appliedDefaults));
    }

    /**
     * Evaluates the default value for a property definition.
     *
     * @param definition the property definition
     * @param currentProperties the current properties map (may include previously applied defaults)
     * @param contextType the context type
     * @param <T> the property value type
     * @return optional containing the default value as a string, or empty if no default
     */
    private <T> Optional<String> evaluateDefault(
            PropertyDefinition<T> definition,
            Map<String, String> currentProperties,
            ValidationContextType contextType) {

        Optional<ConditionalDefaultValue<T>> defaultValueOpt = definition.getDefaultValue();
        if (!defaultValueOpt.isPresent()) {
            return Optional.empty();
        }

        ConditionalDefaultValue<T> defaultValue = defaultValueOpt.get();

        // Create context for evaluating conditional/computed defaults
        PropertyContext context = new DefaultPropertyContext(
                currentProperties,
                contextType,
                converterRegistry
        );

        // Evaluate the default value
        Optional<T> value = defaultValue.computeDefault(context);
        if (!value.isPresent()) {
            return Optional.empty();
        }

        // Convert to string
        return Optional.of(String.valueOf(value.get()));
    }
}
