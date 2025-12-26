package com.cleanconfig.core.impl;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.converter.TypeConverterRegistry;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of PropertyContext.
 *
 * @since 0.1.0
 */
public class DefaultPropertyContext implements PropertyContext {

    private final Map<String, String> properties;
    private final ValidationContextType contextType;
    private final TypeConverterRegistry converterRegistry;
    private final Map<String, String> metadata;

    /**
     * Creates a new property context.
     *
     * @param properties the properties
     * @param contextType the context type
     * @param converterRegistry the converter registry
     */
    public DefaultPropertyContext(
            Map<String, String> properties,
            ValidationContextType contextType,
            TypeConverterRegistry converterRegistry) {
        this(properties, contextType, converterRegistry, Collections.emptyMap());
    }

    /**
     * Creates a new property context with metadata.
     *
     * @param properties the properties
     * @param contextType the context type
     * @param converterRegistry the converter registry
     * @param metadata additional metadata
     */
    public DefaultPropertyContext(
            Map<String, String> properties,
            ValidationContextType contextType,
            TypeConverterRegistry converterRegistry,
            Map<String, String> metadata) {
        this.properties = Objects.requireNonNull(properties, "Properties cannot be null");
        this.contextType = Objects.requireNonNull(contextType, "Context type cannot be null");
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    @Override
    public Optional<String> getProperty(String propertyName) {
        return Optional.ofNullable(properties.get(propertyName));
    }

    @Override
    public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
        String value = properties.get(propertyName);
        if (value == null) {
            return Optional.empty();
        }
        return converterRegistry.convert(value, targetType);
    }

    @Override
    public Map<String, String> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public ValidationContextType getContextType() {
        return contextType;
    }

    @Override
    public Optional<String> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }
}
