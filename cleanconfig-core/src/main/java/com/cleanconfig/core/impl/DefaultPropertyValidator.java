package com.cleanconfig.core.impl;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import com.cleanconfig.core.validation.PropertyGroup;
import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Default implementation of PropertyValidator.
 *
 * <p>This implementation validates properties in dependency order using topological sort
 * to ensure that properties are validated after their dependencies.
 *
 * <p>This class is final to prevent finalizer attacks when constructor throws exceptions.
 *
 * @since 0.1.0
 */
public final class DefaultPropertyValidator implements PropertyValidator {

    private final PropertyRegistry registry;
    private final TypeConverterRegistry converterRegistry;
    private final List<String> validationOrder;

    /**
     * Creates a new validator with the given registry.
     *
     * @param registry the property registry
     */
    public DefaultPropertyValidator(PropertyRegistry registry) {
        this(registry, TypeConverterRegistry.getInstance());
    }

    /**
     * Creates a new validator with the given registry and converter registry.
     *
     * @param registry the property registry
     * @param converterRegistry the type converter registry
     */
    public DefaultPropertyValidator(PropertyRegistry registry, TypeConverterRegistry converterRegistry) {
        this.registry = Objects.requireNonNull(registry, "Property registry cannot be null");
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
        this.validationOrder = computeValidationOrder();
    }

    @Override
    public ValidationResult validate(Map<String, String> properties) {
        Objects.requireNonNull(properties, "Properties cannot be null");

        PropertyContext context = new DefaultPropertyContext(properties, converterRegistry);

        // Functional approach: stream over validation order and collect errors
        List<ValidationError> errors = validationOrder.stream()
                .map(registry::getProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(definition -> validateProperty(definition, properties.get(definition.getName()), context))
                .flatMap(result -> result.getErrors().stream())
                .collect(java.util.stream.Collectors.toList());

        // Validate unknown properties
        List<ValidationError> unknownErrors = properties.entrySet().stream()
                .filter(entry -> !registry.isDefined(entry.getKey()))
                .map(entry -> ValidationError.builder()
                        .propertyName(entry.getKey())
                        .actualValue(entry.getValue())
                        .errorMessage("Unknown property")
                        .expectedValue("Property is not defined in the registry")
                        .build())
                .collect(java.util.stream.Collectors.toList());

        errors.addAll(unknownErrors);

        // Validate property groups
        List<ValidationError> groupErrors = registry.getAllPropertyGroups().stream()
                .map(group -> validatePropertyGroup(group, properties))
                .flatMap(result -> result.getErrors().stream())
                .collect(java.util.stream.Collectors.toList());

        errors.addAll(groupErrors);

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    @Override
    public ValidationResult validateProperty(String propertyName, String value, Map<String, String> properties) {
        Objects.requireNonNull(propertyName, "Property name cannot be null");
        Objects.requireNonNull(properties, "Properties cannot be null");

        PropertyContext context = new DefaultPropertyContext(properties, converterRegistry);

        return registry.getProperty(propertyName)
                .map(definition -> validateProperty(definition, value, context))
                .orElseGet(() -> ValidationResult.failure(ValidationError.builder()
                        .propertyName(propertyName)
                        .actualValue(value)
                        .errorMessage("Unknown property")
                        .expectedValue("Property is not defined in the registry")
                        .build()));
    }

    @Override
    public ValidationResult validatePropertyGroup(PropertyGroup group, Map<String, String> properties) {
        Objects.requireNonNull(group, "Property group cannot be null");
        Objects.requireNonNull(properties, "Properties cannot be null");

        PropertyContext context = new DefaultPropertyContext(properties, converterRegistry);

        // Validate all rules in the group
        List<ValidationError> errors = group.getRules().stream()
                .map(rule -> rule.validate(
                        group.getPropertyNames().toArray(new String[0]),
                        context
                ))
                .flatMap(result -> result.getErrors().stream())
                .collect(java.util.stream.Collectors.toList());

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Validates a single property using functional composition.
     */
    private <T> ValidationResult validateProperty(
            PropertyDefinition<T> definition,
            String value,
            PropertyContext context) {

        // Check required using Optional pattern
        if (definition.isRequired() && (value == null || value.isEmpty())) {
            return ValidationResult.failure(ValidationError.builder()
                    .propertyName(definition.getName())
                    .actualValue(value)
                    .errorMessage("Required property is missing")
                    .expectedValue("Non-null value")
                    .build());
        }

        // Use monadic chain for validation
        return Optional.ofNullable(value)
                .filter(v -> !v.isEmpty())
                .flatMap(v -> convertAndValidate(definition, v, context))
                .orElse(ValidationResult.success());
    }

    /**
     * Higher-order function: converts value and applies validation using monadic composition.
     */
    private <T> Optional<ValidationResult> convertAndValidate(
            PropertyDefinition<T> definition,
            String value,
            PropertyContext context) {

        // Convert value to target type
        Optional<T> convertedValue = converterRegistry.convert(value, definition.getType());

        if (!convertedValue.isPresent()) {
            return Optional.of(ValidationResult.failure(ValidationError.builder()
                    .propertyName(definition.getName())
                    .actualValue(value)
                    .errorMessage("Type conversion failed")
                    .expectedValue("Value of type " + definition.getType().getSimpleName())
                    .build()));
        }

        // Apply validation rule using monadic composition
        return Optional.of(
                definition.getValidationRule()
                        .map(rule -> rule.validate(definition.getName(), convertedValue.get(), context))
                        .orElse(ValidationResult.success())
        );
    }

    /**
     * Computes the validation order using topological sort.
     */
    private List<String> computeValidationOrder() {
        // Build dependency graph using streams
        Map<String, Set<String>> dependencies = registry.getAllProperties().stream()
                .collect(java.util.stream.Collectors.toMap(
                        PropertyDefinition::getName,
                        definition -> definition.getDependsOnForValidation().stream()
                                .filter(registry::isDefined)
                                .collect(java.util.stream.Collectors.toSet())
                ));

        Set<String> allProperties = registry.getAllPropertyNames().stream()
                .collect(java.util.stream.Collectors.toSet());

        // Topological sort using Kahn's algorithm
        List<String> sorted = new ArrayList<>();
        Map<String, Integer> inDegree = allProperties.stream()
                .collect(java.util.stream.Collectors.toMap(
                        property -> property,
                        property -> dependencies.get(property).size()
                ));

        // Queue of nodes with no dependencies (in-degree 0)
        Queue<String> queue = allProperties.stream()
                .filter(property -> inDegree.get(property) == 0)
                .collect(java.util.stream.Collectors.toCollection(LinkedList::new));

        // Process queue
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);

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

        // Check for cycles (should not happen if builder validated correctly)
        if (sorted.size() != allProperties.size()) {
            throw new IllegalStateException(
                    "Circular dependency detected in property validation dependencies");
        }

        return sorted;
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
}
