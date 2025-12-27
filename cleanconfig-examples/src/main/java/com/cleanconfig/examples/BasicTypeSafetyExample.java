package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating type-safe property definitions.
 *
 * <p>This example shows how CleanConfig provides compile-time type safety
 * for configuration properties, eliminating common errors from untyped
 * string-based configuration.
 */
public class BasicTypeSafetyExample {

    public static void main(String[] args) {
        System.out.println("=== Type-Safe Properties Example ===\n");

        // Define type-safe properties with validation
        PropertyDefinition<Integer> serverPort = PropertyDefinition.builder(Integer.class)
                .name("server.port")
                .description("HTTP server port")
                .defaultValue(8080)
                .validationRule(Rules.port())
                .required(true)
                .build();

        PropertyDefinition<String> serverHost = PropertyDefinition.builder(String.class)
                .name("server.host")
                .description("Server hostname")
                .defaultValue("localhost")
                .validationRule(Rules.notBlank())
                .build();

        PropertyDefinition<Integer> maxConnections = PropertyDefinition.builder(Integer.class)
                .name("server.maxConnections")
                .description("Maximum concurrent connections")
                .defaultValue(100)
                .validationRule(Rules.positive())
                .build();

        // Create registry
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(serverPort)
                .register(serverHost)
                .register(maxConnections)
                .build();

        // Create validator
        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Example 1: Valid properties
        System.out.println("Example 1: Valid properties");
        Map<String, String> validProps = new HashMap<>();
        validProps.put("server.port", "9000");
        validProps.put("server.host", "api.example.com");
        validProps.put("server.maxConnections", "200");

        ValidationResult result1 = validator.validate(validProps);
        if (result1.isValid()) {
            System.out.println("✓ All properties are valid!");
            System.out.println("  server.port: " + validProps.get("server.port"));
            System.out.println("  server.host: " + validProps.get("server.host"));
            System.out.println("  server.maxConnections: " + validProps.get("server.maxConnections"));
        }

        System.out.println();

        // Example 2: Invalid port (out of range)
        System.out.println("Example 2: Invalid port value");
        Map<String, String> invalidProps = new HashMap<>();
        invalidProps.put("server.port", "99999");  // Port must be 1-65535
        invalidProps.put("server.host", "localhost");

        ValidationResult result2 = validator.validate(invalidProps);
        if (!result2.isValid()) {
            System.out.println("✗ Validation failed:");
            result2.getErrors().forEach(error ->
                System.out.println("  " + error.getPropertyName() + ": " + error.getErrorMessage())
            );
        }

        System.out.println();

        // Example 3: Invalid type
        System.out.println("Example 3: Invalid type (string instead of integer)");
        Map<String, String> typeError = new HashMap<>();
        typeError.put("server.port", "not-a-number");
        typeError.put("server.host", "localhost");

        ValidationResult result3 = validator.validate(typeError);
        if (!result3.isValid()) {
            System.out.println("✗ Validation failed:");
            result3.getErrors().forEach(error ->
                System.out.println("  " + error.getPropertyName() + ": " + error.getErrorMessage())
            );
        }

        System.out.println("\n=== Type safety prevents runtime errors! ===");
    }
}
