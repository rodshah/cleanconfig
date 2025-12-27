package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating dependency-aware validation.
 *
 * <p>This example shows how properties can depend on each other for validation,
 * ensuring that properties are validated in the correct order and can reference
 * each other's values during validation.
 */
public class DependencyAwareValidationExample {

    public static void main(String[] args) {
        System.out.println("=== Dependency-Aware Validation Example ===\n");

        // Example: Kubernetes-style resource limits
        // CPU/Memory request must be <= limit

        PropertyDefinition<String> cpuRequest = PropertyDefinition.builder(String.class)
                .name("resources.cpu.request")
                .description("CPU request (e.g., 100m, 1)")
                .validationRule(Rules.notBlank())
                .required(true)
                .build();

        PropertyDefinition<String> cpuLimit = PropertyDefinition.builder(String.class)
                .name("resources.cpu.limit")
                .description("CPU limit (must be >= request)")
                .dependsOnForValidation("resources.cpu.request")  // Validated AFTER request
                .validationRule(Rules.notBlank().and((name, value, context) ->
                    validateLimit(name, value, "resources.cpu.request", context)
                ))
                .required(true)
                .build();

        PropertyDefinition<String> memoryRequest = PropertyDefinition.builder(String.class)
                .name("resources.memory.request")
                .description("Memory request (e.g., 128Mi, 1Gi)")
                .validationRule(Rules.notBlank())
                .required(true)
                .build();

        PropertyDefinition<String> memoryLimit = PropertyDefinition.builder(String.class)
                .name("resources.memory.limit")
                .description("Memory limit (must be >= request)")
                .dependsOnForValidation("resources.memory.request")  // Validated AFTER request
                .validationRule(Rules.notBlank().and((name, value, context) ->
                    validateLimit(name, value, "resources.memory.request", context)
                ))
                .required(true)
                .build();

        // Build registry - circular dependencies are detected at build time
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(cpuRequest)
                .register(cpuLimit)
                .register(memoryRequest)
                .register(memoryLimit)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Scenario 1: Valid configuration (limit >= request)
        System.out.println("Scenario 1: Valid configuration");
        Map<String, String> validConfig = new HashMap<>();
        validConfig.put("resources.cpu.request", "500m");
        validConfig.put("resources.cpu.limit", "1000m");
        validConfig.put("resources.memory.request", "512Mi");
        validConfig.put("resources.memory.limit", "1Gi");

        ValidationResult result1 = validator.validate(validConfig);
        printResult(result1, validConfig);

        System.out.println();

        // Scenario 2: Invalid - CPU limit < request
        System.out.println("Scenario 2: CPU limit less than request");
        Map<String, String> invalidCpu = new HashMap<>();
        invalidCpu.put("resources.cpu.request", "1000m");
        invalidCpu.put("resources.cpu.limit", "500m");  // Less than request!
        invalidCpu.put("resources.memory.request", "512Mi");
        invalidCpu.put("resources.memory.limit", "1Gi");

        ValidationResult result2 = validator.validate(invalidCpu);
        printResult(result2, invalidCpu);

        System.out.println();

        // Scenario 3: Invalid - Memory limit < request
        System.out.println("Scenario 3: Memory limit less than request");
        Map<String, String> invalidMemory = new HashMap<>();
        invalidMemory.put("resources.cpu.request", "500m");
        invalidMemory.put("resources.cpu.limit", "1000m");
        invalidMemory.put("resources.memory.request", "1Gi");
        invalidMemory.put("resources.memory.limit", "512Mi");  // Less than request!

        ValidationResult result3 = validator.validate(invalidMemory);
        printResult(result3, invalidMemory);

        System.out.println();

        // Scenario 4: Multiple validation errors
        System.out.println("Scenario 4: Multiple validation errors");
        Map<String, String> multipleErrors = new HashMap<>();
        multipleErrors.put("resources.cpu.request", "2000m");
        multipleErrors.put("resources.cpu.limit", "1000m");  // Less than request
        multipleErrors.put("resources.memory.request", "2Gi");
        multipleErrors.put("resources.memory.limit", "1Gi");  // Less than request

        ValidationResult result4 = validator.validate(multipleErrors);
        printResult(result4, multipleErrors);

        System.out.println("\n=== Dependencies ensure correct validation order! ===");
    }

    private static ValidationResult validateLimit(
            String limitName,
            String limitValue,
            String requestName,
            PropertyContext context) {

        String requestValue = context.getProperty(requestName).orElse("");

        // Simple numeric comparison (in production, parse units properly)
        double limit = parseResourceValue(limitValue);
        double request = parseResourceValue(requestValue);

        if (limit < request) {
            return ValidationResult.failure(
                com.cleanconfig.core.validation.ValidationError.builder()
                        .propertyName(limitName)
                        .actualValue(limitValue)
                        .expectedValue(">= " + requestValue)
                        .errorMessage("Limit must be greater than or equal to request")
                        .build()
            );
        }

        return ValidationResult.success();
    }

    private static double parseResourceValue(String value) {
        // Simplified parsing (in production, handle all Kubernetes units)
        if (value.endsWith("m")) {
            return Double.parseDouble(value.substring(0, value.length() - 1));
        } else if (value.endsWith("Mi")) {
            return Double.parseDouble(value.substring(0, value.length() - 2)) * 1024;
        } else if (value.endsWith("Gi")) {
            return Double.parseDouble(value.substring(0, value.length() - 2)) * 1024 * 1024;
        } else {
            return Double.parseDouble(value) * 1000;
        }
    }

    private static void printResult(ValidationResult result, Map<String, String> config) {
        if (result.isValid()) {
            System.out.println("✓ Configuration is valid!");
            config.forEach((key, value) ->
                System.out.println("  " + key + ": " + value)
            );
        } else {
            System.out.println("✗ Validation failed:");
            result.getErrors().forEach(error ->
                System.out.println("  " + error.getPropertyName() + ": "
                        + error.getErrorMessage()
                        + " (expected: " + error.getExpectedValue() + ", actual: " + error.getActualValue() + ")")
            );
        }
    }
}
