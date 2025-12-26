package com.cleanconfig.core.impl;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * @since 0.1.0
 */
public class DefaultPropertyValidator implements PropertyValidator {

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
        return validate(properties, ValidationContextType.STARTUP);
    }

    @Override
    public ValidationResult validate(Map<String, String> properties, ValidationContextType contextType) {
        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(contextType, "Context type cannot be null");

        PropertyContext context = new DefaultPropertyContext(properties, contextType, converterRegistry);
        List<ValidationError> errors = new ArrayList<>();

        // Validate in dependency order
        for (String propertyName : validationOrder) {
            registry.getProperty(propertyName).ifPresent(definition -> {
                String value = properties.get(propertyName);
                ValidationResult result = validatePropertyInternal(definition, value, context);
                errors.addAll(result.getErrors());
            });
        }

        // Validate properties not in registry (unknown properties)
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!registry.isDefined(entry.getKey())) {
                errors.add(ValidationError.builder()
                        .propertyName(entry.getKey())
                        .actualValue(entry.getValue())
                        .errorMessage("Unknown property")
                        .expectedValue("Property is not defined in the registry")
                        .build());
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    @Override
    public ValidationResult validateProperty(String propertyName, String value, Map<String, String> properties) {
        Objects.requireNonNull(propertyName, "Property name cannot be null");
        Objects.requireNonNull(properties, "Properties cannot be null");

        PropertyContext context = new DefaultPropertyContext(properties, ValidationContextType.STARTUP, converterRegistry);

        return registry.getProperty(propertyName)
                .map(definition -> validatePropertyInternal(definition, value, context))
                .orElseGet(() -> ValidationResult.failure(ValidationError.builder()
                        .propertyName(propertyName)
                        .actualValue(value)
                        .errorMessage("Unknown property")
                        .expectedValue("Property is not defined in the registry")
                        .build()));
    }

    /**
     * Validates a single property value against its definition.
     */
    private <T> ValidationResult validatePropertyInternal(
            PropertyDefinition<T> definition,
            String value,
            PropertyContext context) {

        // Check if required
        if (definition.isRequired() && (value == null || value.isEmpty())) {
            return ValidationResult.failure(ValidationError.builder()
                    .propertyName(definition.getName())
                    .actualValue(value)
                    .errorMessage("Required property is missing")
                    .expectedValue("Non-null value")
                    .build());
        }

        // Use default if not provided
        if (value == null || value.isEmpty()) {
            return ValidationResult.success();
        }

        // Convert value to target type
        Optional<T> convertedValue = converterRegistry.convert(value, definition.getType());
        if (!convertedValue.isPresent()) {
            return ValidationResult.failure(ValidationError.builder()
                    .propertyName(definition.getName())
                    .actualValue(value)
                    .errorMessage("Type conversion failed")
                    .expectedValue("Value of type " + definition.getType().getSimpleName())
                    .build());
        }

        // Apply validation rule
        return definition.getValidationRule()
                .map(rule -> rule.validate(definition.getName(), convertedValue.get(), context))
                .orElse(ValidationResult.success());
    }

    /**
     * Computes the validation order using topological sort.
     */
    private List<String> computeValidationOrder() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        Set<String> allProperties = new HashSet<>();

        // Build dependency graph - only include dependencies that exist in the registry
        for (PropertyDefinition<?> definition : registry.getAllProperties()) {
            String propertyName = definition.getName();
            allProperties.add(propertyName);

            Set<String> validDeps = new HashSet<>();
            for (String dep : definition.getDependsOnForValidation()) {
                if (registry.isDefined(dep)) {
                    validDeps.add(dep);
                }
            }
            dependencies.put(propertyName, validDeps);
        }

        // Topological sort using Kahn's algorithm
        List<String> sorted = new ArrayList<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // Calculate in-degrees: for each property, count how many dependencies it has
        for (String property : allProperties) {
            inDegree.put(property, dependencies.get(property).size());
        }

        // Queue of nodes with no dependencies (in-degree 0)
        Queue<String> queue = new LinkedList<>();
        for (String property : allProperties) {
            if (inDegree.get(property) == 0) {
                queue.add(property);
            }
        }

        // Process queue
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);

            // Find all properties that depend on the current property
            for (String dependent : findDependents(current, dependencies)) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
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
        Set<String> dependents = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            if (entry.getValue().contains(property)) {
                dependents.add(entry.getKey());
            }
        }
        return dependents;
    }
}
