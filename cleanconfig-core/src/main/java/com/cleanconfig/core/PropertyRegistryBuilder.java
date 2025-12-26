package com.cleanconfig.core;

import com.cleanconfig.core.validation.PropertyGroup;

import java.util.LinkedHashMap;
import java.util.Map;
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
 *     .registerGroup(databaseGroup)
 *     .build();
 * </pre>
 *
 * @since 0.1.0
 */
public class PropertyRegistryBuilder {

    private final Map<String, PropertyDefinition<?>> properties = new LinkedHashMap<>();
    private final Map<String, PropertyGroup> propertyGroups = new LinkedHashMap<>();

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
     * Registers a property group.
     *
     * @param group the property group to register
     * @return this builder
     * @throws IllegalArgumentException if a group with the same name is already registered
     * @since 0.2.0
     */
    public PropertyRegistryBuilder registerGroup(PropertyGroup group) {
        Objects.requireNonNull(group, "Property group cannot be null");
        Objects.requireNonNull(group.getName(), "Group name cannot be null");

        if (propertyGroups.containsKey(group.getName())) {
            throw new IllegalArgumentException(
                    "Property group '" + group.getName() + "' is already registered");
        }

        propertyGroups.put(group.getName(), group);
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
        return new DefaultPropertyRegistry(
                new LinkedHashMap<>(properties),
                new LinkedHashMap<>(propertyGroups)
        );
    }

    /**
     * Validates that all dependency references exist.
     */
    private void validateDependencies() {
        properties.values().stream()
                .flatMap(property -> property.getDependsOnForValidation().stream()
                        .filter(dependency -> !properties.containsKey(dependency))
                        .map(dependency -> new IllegalStateException(
                                "Property '" + property.getName() + "' depends on undefined property '" + dependency + "'")))
                .findFirst()
                .ifPresent(exception -> {
                    throw exception;
                });
    }

    /**
     * Detects circular dependencies using topological sort.
     */
    private void detectCircularDependencies() {
        // Build dependency graph
        Map<String, Set<String>> dependencies = properties.values().stream()
                .collect(java.util.stream.Collectors.toMap(
                        PropertyDefinition::getName,
                        definition -> definition.getDependsOnForValidation().stream()
                                .filter(properties::containsKey)
                                .collect(java.util.stream.Collectors.toSet())
                ));

        Set<String> allProperties = new HashSet<>(properties.keySet());

        // Topological sort using Kahn's algorithm
        Map<String, Integer> inDegree = allProperties.stream()
                .collect(java.util.stream.Collectors.toMap(
                        property -> property,
                        property -> dependencies.get(property).size()
                ));

        Queue<String> queue = allProperties.stream()
                .filter(property -> inDegree.get(property) == 0)
                .collect(java.util.stream.Collectors.toCollection(LinkedList::new));

        int processed = 0;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            processed++;

            // Find all properties that depend on the current property
            findDependents(current, dependencies).stream()
                    .forEach(dependent -> {
                        int newDegree = inDegree.get(dependent) - 1;
                        inDegree.put(dependent, newDegree);
                        if (newDegree == 0) {
                            queue.add(dependent);
                        }
                    });
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
        return dependencies.entrySet().stream()
                .filter(entry -> entry.getValue().contains(property))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Default implementation of PropertyRegistry.
     */
    private static class DefaultPropertyRegistry implements PropertyRegistry {
        private final Map<String, PropertyDefinition<?>> properties;
        private final Map<String, PropertyGroup> propertyGroups;

        DefaultPropertyRegistry(
                Map<String, PropertyDefinition<?>> properties,
                Map<String, PropertyGroup> propertyGroups) {
            this.properties = properties;
            this.propertyGroups = propertyGroups;
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

        @Override
        public Optional<PropertyGroup> getPropertyGroup(String groupName) {
            return Optional.ofNullable(propertyGroups.get(groupName));
        }

        @Override
        public Collection<PropertyGroup> getAllPropertyGroups() {
            return Collections.unmodifiableCollection(propertyGroups.values());
        }
    }
}
