package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.format.JsonValidationFormatter;
import com.cleanconfig.core.validation.format.TextValidationFormatter;
import com.cleanconfig.core.validation.format.ValidationFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates enhanced error messages and validation formatters.
 *
 * <p>Shows:
 * <ul>
 *   <li>Error messages with actual/expected values</li>
 *   <li>Text formatter for human-readable console output</li>
 *   <li>JSON formatter for machine-readable output</li>
 *   <li>Multiple validation errors with clear formatting</li>
 * </ul>
 */
public class ErrorFormattingExample {

    public static void main(String[] args) {
        System.out.println("=== CleanConfig Error Formatting Example ===\n");

        example1_TextFormatter();
        System.out.println("\n" + "=".repeat(60) + "\n");

        example2_JsonFormatter();
    }

    /**
     * Example 1: Text Formatter for Console Output
     */
    private static void example1_TextFormatter() {
        System.out.println("Example 1: Text Formatter (Human-Readable)");
        System.out.println("------------------------------------------");

        // Create registry with validation rules
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(Integer.class)
                        .name("server.port")
                        .defaultValue(8080)
                        .validationRule(Rules.integerBetween(1024, 65535))
                        .category(PropertyCategory.GENERAL)
                        .build())
                .register(PropertyDefinition.builder(String.class)
                        .name("db.url")
                        .defaultValue("jdbc:postgresql://localhost:5432/mydb")
                        .validationRule(Rules.url())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .register(PropertyDefinition.builder(String.class)
                        .name("admin.email")
                        .defaultValue("admin@example.com")
                        .validationRule(Rules.email())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .build();

        // Invalid properties (multiple errors)
        Map<String, String> invalidProperties = new HashMap<>();
        invalidProperties.put("server.port", "80");          // Too low
        invalidProperties.put("db.url", "not-a-url");        // Invalid URL
        invalidProperties.put("admin.email", "invalid");     // Invalid email

        // Validate
        PropertyValidator validator = new DefaultPropertyValidator(registry);
        ValidationResult result = validator.validate(invalidProperties);

        // Format with TextFormatter
        ValidationFormatter textFormatter = new TextValidationFormatter();
        String formattedText = textFormatter.format(result);

        System.out.println(formattedText);
    }

    /**
     * Example 2: JSON Formatter for Machine-Readable Output
     */
    private static void example2_JsonFormatter() {
        System.out.println("Example 2: JSON Formatter (Machine-Readable)");
        System.out.println("---------------------------------------------");

        // Create a simple registry
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(Integer.class)
                        .name("server.port")
                        .defaultValue(8080)
                        .validationRule(Rules.port())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .build();

        // Invalid property
        Map<String, String> properties = new HashMap<>();
        properties.put("server.port", "-1");

        // Validate
        PropertyValidator validator = new DefaultPropertyValidator(registry);
        ValidationResult result = validator.validate(properties);

        // Format with JsonFormatter
        ValidationFormatter jsonFormatter = new JsonValidationFormatter();
        String formattedJson = jsonFormatter.format(result);

        System.out.println(formattedJson);
    }
}
