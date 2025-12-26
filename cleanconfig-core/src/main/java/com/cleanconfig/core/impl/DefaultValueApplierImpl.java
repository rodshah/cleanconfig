package com.cleanconfig.core.impl;

import com.cleanconfig.core.ConditionalDefaultValue;
import com.cleanconfig.core.DefaultApplicationInfo;
import com.cleanconfig.core.DefaultApplicationResult;
import com.cleanconfig.core.DefaultValueApplier;
import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.converter.TypeConverterRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        Objects.requireNonNull(userProperties, "User properties cannot be null");

        // Create property context factory
        Function<Map<String, String>, PropertyContext> contextFactory =
                props -> new DefaultPropertyContext(props, converterRegistry);

        return applyDefaultsWithContext(userProperties, contextFactory);
    }

    /**
     * Applies defaults using functional composition and immutable transformations.
     *
     * @param userProperties the user properties
     * @param contextFactory factory for creating property contexts
     * @return the application result
     */
    private DefaultApplicationResult applyDefaultsWithContext(
            Map<String, String> userProperties,
            Function<Map<String, String>, PropertyContext> contextFactory) {

        // Fold over property definitions, accumulating results
        Map<String, String> finalProperties = new LinkedHashMap<>(userProperties);
        Map<String, String> appliedDefaults = new LinkedHashMap<>();

        // Higher-order function: creates default applicator for a single property
        BiFunction<PropertyDefinition<?>, Map<String, String>, Optional<Map.Entry<String, String>>> applyDefault =
                (definition, currentProps) -> applyDefaultForProperty(definition, currentProps, contextFactory);

        // Stream-based functional application
        registry.getAllProperties().stream()
                .filter(definition -> !userProperties.containsKey(definition.getName()))
                .forEach(definition -> applyDefault.apply(definition, finalProperties)
                        .ifPresent(entry -> {
                            finalProperties.put(entry.getKey(), entry.getValue());
                            appliedDefaults.put(entry.getKey(), entry.getValue());
                        }));

        return new DefaultApplicationResult(finalProperties, new DefaultApplicationInfo(appliedDefaults));
    }

    /**
     * Higher-order function: applies default for a single property.
     *
     * @param definition the property definition
     * @param currentProperties current property map
     * @param contextFactory context factory
     * @param <T> property type
     * @return optional entry with property name and default value
     */
    private <T> Optional<Map.Entry<String, String>> applyDefaultForProperty(
            PropertyDefinition<T> definition,
            Map<String, String> currentProperties,
            Function<Map<String, String>, PropertyContext> contextFactory) {

        return definition.getDefaultValue()
                .flatMap(defaultValue -> evaluateDefault(defaultValue, currentProperties, contextFactory))
                .map(value -> Map.entry(definition.getName(), value));
    }

    /**
     * Evaluates a conditional default value using monadic composition.
     *
     * @param defaultValue the conditional default
     * @param currentProperties current properties
     * @param contextFactory context factory
     * @param <T> property type
     * @return optional string value
     */
    private <T> Optional<String> evaluateDefault(
            ConditionalDefaultValue<T> defaultValue,
            Map<String, String> currentProperties,
            Function<Map<String, String>, PropertyContext> contextFactory) {

        return Optional.of(currentProperties)
                .map(contextFactory)
                .flatMap(defaultValue::computeDefault)
                .map(String::valueOf);
    }
}
