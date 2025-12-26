package com.cleanconfig.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Optional;
import java.util.Collection;
import java.util.Collections;

/**
 * Builder for creating property registries.
 *
 * <p>The builder validates property definitions as they are registered and detects
 * circular dependencies before constructing the registry.
 *
 * <p>Example usage:
 * <pre>
 * PropertyRegistry registry = PropertyRegistry.builder()
 *     .register(serverPortProperty)
 *     .register(serverHostProperty)
 *     .build();
 * </pre>
 *
 * @since 0.1.0
 */
public class PropertyRegistryBuilder {

    private final Map<String, PropertyDefinition<?>> properties = new LinkedHashMap<>();

    /**
     * Registers a property definition.
     *
     * @param property the property definition to register
     * @param <T> the property value type
     * @return this builder
     * @throws IllegalArgumentException if a property with the same name is already registered
     */
    public <T> PropertyRegistryBuilder register(PropertyDefinition<T> property) {
        Objects.requireNonNull(property, "Property definition cannot be null");
        Objects.requireNonNull(property.getName(), "Property name cannot be null");

        if (properties.containsKey(property.getName())) {
            throw new IllegalArgumentException(
                    "Property '" + property.getName() + "' is already registered");
        }

        properties.put(property.getName(), property);
        return this;
    }

    /**
     * Builds the property registry.
     *
     * @return the property registry
     * @throws IllegalStateException if circular dependencies are detected
     */
    public PropertyRegistry build() {
        validateNoDependencies();
        detectCircularDependencies();
        return new DefaultPropertyRegistry(new LinkedHashMap<>(properties));
    }

    /**
     * Validates that all dependency references exist.
     */
    private void validateDependencies() {
        for (PropertyDefinition<?> property : properties.values()) {
            for (String dependency : property.getDependsOnForValidation()) {
                if (!properties.containsKey(dependency)) {
                    throw new IllegalStateException(
                            "Property '" + property.getName() + "' depends on undefined property '" + dependency + "'");
                }
            }
        }
    }

    /**
     * Detects circular dependencies using topological sort.
     */
    private void detectCircularDependencies() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        Set<String> allProperties = new HashSet<>();

        // Build dependency graph
        for (PropertyDefinition<?> definition : properties.values()) {
            String propertyName = definition.getName();
            allProperties.add(propertyName);

            Set<String> deps = new HashSet<>();
            for (String dep : definition.getDependsOnForValidation()) {
                if (properties.containsKey(dep)) {
                    deps.add(dep);
                }
            }
            dependencies.put(propertyName, deps);
        }

        // Topological sort using Kahn's algorithm
        Map<String, Integer> inDegree = new HashMap<>();
        for (String property : allProperties) {
            inDegree.put(property, dependencies.get(property).size());
        }

        Queue<String> queue = new LinkedList<>();
        for (String property : allProperties) {
            if (inDegree.get(property) == 0) {
                queue.add(property);
            }
        }

        int processed = 0;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            processed++;

            // Find all properties that depend on the current property
            for (String dependent : findDependents(current, dependencies)) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (processed != allProperties.size()) {
            throw new IllegalStateException(
                    "Circular dependency detected in property validation dependencies");
        }
    }

    /**
     * Validates that no properties have dependencies (for no-dependency build mode).
     */
    private void validateNoDependencies() {
        for (PropertyDefinition<?> property : properties.values()) {
            if (!property.getDependsOnForValidation().isEmpty()) {
                // Dependencies exist, validate them
                validateDependencies();
                return;
            }
        }
    }

    /**
     * Finds all properties that depend on the given property.
     */
    private Set<String> findDependents(String property, Map<String, Set<String>> dependencies) {
        Set<String> dependents = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            if (entry.getValue().contains(property)) {
                dependents.add(entry.getKey());
            }
        }
        return dependents;
    }

    /**
     * Default implementation of PropertyRegistry.
     */
    private static class DefaultPropertyRegistry implements PropertyRegistry {
        private final Map<String, PropertyDefinition<?>> properties;

        DefaultPropertyRegistry(Map<String, PropertyDefinition<?>> properties) {
            this.properties = properties;
        }

        @Override
        public Optional<PropertyDefinition<?>> getProperty(String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }

        @Override
        public boolean isDefined(String propertyName) {
            return properties.containsKey(propertyName);
        }

        @Override
        public Collection<PropertyDefinition<?>> getAllProperties() {
            return Collections.unmodifiableCollection(properties.values());
        }

        @Override
        public Collection<String> getAllPropertyNames() {
            return Collections.unmodifiableSet(properties.keySet());
        }
    }
}
