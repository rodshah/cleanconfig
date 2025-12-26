package com.cleanconfig.core;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry for managing property definitions.
 *
 * <p>The property registry stores all property definitions and provides methods for
 * querying and retrieving them. It ensures that property names are unique and validates
 * dependencies between properties at registration time.
 *
 * <p>Example usage:
 * <pre>
 * PropertyRegistry registry = PropertyRegistry.builder()
 *     .register(serverPortProperty)
 *     .register(serverHostProperty)
 *     .build();
 *
 * Optional&lt;PropertyDefinition&lt;?&gt;&gt; property = registry.getProperty("server.port");
 * </pre>
 *
 * @since 0.1.0
 */
public interface PropertyRegistry {

    /**
     * Creates a new builder for constructing a property registry.
     *
     * @return a new property registry builder
     */
    static PropertyRegistryBuilder builder() {
        return new PropertyRegistryBuilder();
    }

    /**
     * Gets a property definition by name.
     *
     * @param propertyName the property name
     * @return the property definition, or empty if not found
     */
    Optional<PropertyDefinition<?>> getProperty(String propertyName);

    /**
     * Checks if a property is defined in this registry.
     *
     * @param propertyName the property name
     * @return true if the property is defined
     */
    boolean isDefined(String propertyName);

    /**
     * Gets all property definitions in this registry.
     *
     * @return all property definitions
     */
    Collection<PropertyDefinition<?>> getAllProperties();

    /**
     * Gets all property names in this registry.
     *
     * @return all property names
     */
    Collection<String> getAllPropertyNames();
}
