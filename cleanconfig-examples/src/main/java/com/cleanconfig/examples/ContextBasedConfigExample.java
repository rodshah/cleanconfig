package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Conditions;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating property-based conditional validation.
 *
 * <p>This example shows how validation rules can adapt based on property values
 * (environment, feature flags, etc.) providing flexible configuration management
 * across different environments and use cases.
 */
public class ContextBasedConfigExample {

    public static void main(String[] args) {
        System.out.println("=== Property-Based Conditional Validation Example ===\n");

        // Properties with conditional validation based on other properties

        PropertyDefinition<String> environment = PropertyDefinition.builder(String.class)
                .name("environment")
                .description("Deployment environment")
                .defaultValue("development")
                .build();

        PropertyDefinition<String> apiKey = PropertyDefinition.builder(String.class)
                .name("api.key")
                .description("API authentication key")
                // Required in production, optional in development
                .validationRule(
                        Rules.notBlank()
                                .onlyIf(Conditions.propertyEquals("environment", "production"))
                )
                .build();

        PropertyDefinition<Boolean> sslEnabled = PropertyDefinition.builder(Boolean.class)
                .name("ssl.enabled")
                .description("Enable SSL/TLS")
                .defaultValue(false)
                .build();

        PropertyDefinition<String> sslCertPath = PropertyDefinition.builder(String.class)
                .name("ssl.certPath")
                .description("Path to SSL certificate")
                // Only validated if SSL is enabled
                .validationRule(
                        Rules.fileExists()
                                .onlyIf(Conditions.propertyEquals("ssl.enabled", "true"))
                )
                .build();

        PropertyDefinition<String> debugMode = PropertyDefinition.builder(String.class)
                .name("debug.enabled")
                .description("Enable debug mode")
                // Must be disabled in production
                .validationRule(
                        Rules.oneOf("false")
                                .onlyIf(Conditions.propertyEquals("environment", "production"))
                )
                .defaultValue("false")
                .build();

        PropertyDefinition<Integer> cacheSize = PropertyDefinition.builder(Integer.class)
                .name("cache.size")
                .description("Cache size in MB")
                .validationRule(Rules.positive())
                .defaultValue(100)
                .build();

        // Build registry
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(environment)
                .register(apiKey)
                .register(sslEnabled)
                .register(sslCertPath)
                .register(debugMode)
                .register(cacheSize)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Scenario 1: Development environment
        System.out.println("Scenario 1: Development environment");
        Map<String, String> devProps = new HashMap<>();
        devProps.put("environment", "development");
        devProps.put("ssl.enabled", "false");
        devProps.put("debug.enabled", "true");  // Allowed in development

        ValidationResult result1 = validator.validate(devProps);
        printResult("Development", result1, devProps);

        System.out.println();

        // Scenario 2: Production (missing API key)
        System.out.println("Scenario 2: Production (missing API key)");
        Map<String, String> prodNoKey = new HashMap<>();
        prodNoKey.put("environment", "production");
        prodNoKey.put("ssl.enabled", "true");
        prodNoKey.put("ssl.certPath", "/etc/ssl/cert.pem");
        prodNoKey.put("debug.enabled", "false");

        ValidationResult result2 = validator.validate(prodNoKey);
        printResult("Production", result2, prodNoKey);

        System.out.println();

        // Scenario 3: Production (complete)
        System.out.println("Scenario 3: Production (complete)");
        Map<String, String> prodComplete = new HashMap<>();
        prodComplete.put("environment", "production");
        prodComplete.put("api.key", "prod-key-abc123");
        prodComplete.put("ssl.enabled", "false");  // File check skipped
        prodComplete.put("debug.enabled", "false");

        ValidationResult result3 = validator.validate(prodComplete);
        printResult("Production", result3, prodComplete);

        System.out.println();

        // Scenario 4: Production with debug enabled (invalid)
        System.out.println("Scenario 4: Production with debug enabled");
        Map<String, String> prodDebug = new HashMap<>();
        prodDebug.put("environment", "production");
        prodDebug.put("api.key", "key123");
        prodDebug.put("debug.enabled", "true");  // Not allowed in production

        ValidationResult result4 = validator.validate(prodDebug);
        printResult("Production Debug", result4, prodDebug);

        System.out.println();

        // Scenario 5: SSL enabled but missing certificate
        System.out.println("Scenario 5: SSL enabled but certificate missing");
        Map<String, String> sslNoCert = new HashMap<>();
        sslNoCert.put("api.key", "key123");
        sslNoCert.put("ssl.enabled", "true");
        sslNoCert.put("ssl.certPath", "/nonexistent/cert.pem");

        ValidationResult result5 = validator.validate(sslNoCert);
        printResult("SSL Without Certificate", result5, sslNoCert);

        System.out.println("\n=== Property-based conditions provide flexible validation! ===");
    }

    private static void printResult(String scenario, ValidationResult result, Map<String, String> config) {
        if (result.isValid()) {
            System.out.println("✓ " + scenario + " - Configuration is valid!");
            config.forEach((key, value) ->
                System.out.println("  " + key + ": " + value)
            );
        } else {
            System.out.println("✗ " + scenario + " - Validation failed:");
            result.getErrors().forEach(error ->
                System.out.println("  " + error.getPropertyName() + ": " + error.getErrorMessage())
            );
        }
    }
}
