package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.hocon.HoconPropertySource;

import java.util.Map;

/**
 * Demonstrates loading HOCON configuration files with CleanConfig.
 *
 * <p>HOCON (Human-Optimized Config Object Notation) is a superset of JSON that
 * supports nesting, substitutions (${references}), durations, and comments.
 * The cleanconfig-hocon module bridges Typesafe Config's HOCON parser with
 * CleanConfig's validation pipeline.</p>
 *
 * <p>Run with:
 * {@code ./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.HoconExample}</p>
 *
 * <h3>Examples covered:</h3>
 * <ol>
 *   <li>Loading HOCON from classpath — nested objects flatten to dot-notation</li>
 *   <li>Substitution resolution — ${variable} references resolved before validation</li>
 *   <li>Array handling — arrays flatten to indexed keys (items.0, items.1)</li>
 *   <li>Validating HOCON properties against a PropertyRegistry</li>
 *   <li>Loading HOCON from an inline string</li>
 * </ol>
 */
public class HoconExample {

    public static void main(String[] args) {
        example1_loadFromClasspath();
        example2_substitutionResolution();
        example3_arrayHandling();
        example4_validateWithRegistry();
        example5_loadFromString();
    }

    // ── Example 1: Load HOCON from classpath ────────────────────────────────

    private static void example1_loadFromClasspath() {
        System.out.println("=== Example 1: Load HOCON from classpath ===\n");

        // HoconPropertySource loads and flattens HOCON to Map<String, String>
        Map<String, String> properties = HoconPropertySource.load("example-hocon.conf");

        // Nested HOCON objects are flattened with dot-notation keys
        System.out.println("Loaded " + properties.size() + " properties from example-hocon.conf\n");
        System.out.println("  server.host           = " + properties.get("server.host"));
        System.out.println("  server.port           = " + properties.get("server.port"));
        System.out.println("  database.pool.max-connections = " + properties.get("database.pool.max-connections"));
        System.out.println("  database.pool.idle-timeout    = " + properties.get("database.pool.idle-timeout"));
        System.out.println("  app.name              = " + properties.get("app.name"));
        System.out.println();
    }

    // ── Example 2: Substitution resolution ──────────────────────────────────

    private static void example2_substitutionResolution() {
        System.out.println("=== Example 2: Substitution resolution ===\n");

        Map<String, String> properties = HoconPropertySource.load("example-hocon.conf");

        // ${server.host} and ${server.port} in the HOCON file are resolved before flattening
        System.out.println("  server.base-url = " + properties.get("server.base-url"));
        System.out.println("  database.url    = " + properties.get("database.url"));
        System.out.println();
        System.out.println("  Both values were computed from ${server.host} substitution");
        System.out.println();
    }

    // ── Example 3: Array handling ───────────────────────────────────────────

    private static void example3_arrayHandling() {
        System.out.println("=== Example 3: Array handling ===\n");

        Map<String, String> properties = HoconPropertySource.load("example-hocon.conf");

        // Arrays flatten to indexed keys: features.0, features.1, etc.
        System.out.println("  app.features.0 = " + properties.get("app.features.0"));
        System.out.println("  app.features.1 = " + properties.get("app.features.1"));
        System.out.println("  app.features.2 = " + properties.get("app.features.2"));
        System.out.println("  app.features.3 = " + properties.get("app.features.3"));
        System.out.println();
        System.out.println("  allowed-origins:");
        System.out.println("    app.allowed-origins.0 = " + properties.get("app.allowed-origins.0"));
        System.out.println("    app.allowed-origins.1 = " + properties.get("app.allowed-origins.1"));
        System.out.println();
    }

    // ── Example 4: Validate HOCON properties ────────────────────────────────

    private static void example4_validateWithRegistry() {
        System.out.println("=== Example 4: Validate HOCON properties against a registry ===\n");

        // Define expected properties with validation rules
        PropertyDefinition<Integer> serverPort = PropertyDefinition.builder(Integer.class)
                .name("server.port")
                .description("HTTP server port")
                .required(true)
                .validationRule(Rules.port())
                .build();

        PropertyDefinition<String> serverHost = PropertyDefinition.builder(String.class)
                .name("server.host")
                .description("Server hostname")
                .required(true)
                .validationRule(Rules.notBlank())
                .build();

        PropertyDefinition<Integer> maxConnections = PropertyDefinition.builder(Integer.class)
                .name("database.pool.max-connections")
                .description("Maximum database connections")
                .required(true)
                .validationRule(Rules.integerBetween(1, 100))
                .build();

        PropertyDefinition<String> kafkaBrokers = PropertyDefinition.builder(String.class)
                .name("kafka.brokers")
                .description("Kafka bootstrap servers")
                .required(true)
                .validationRule(Rules.notBlank())
                .build();

        // Build registry and validator
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(serverPort)
                .register(serverHost)
                .register(maxConnections)
                .register(kafkaBrokers)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Load HOCON and validate
        Map<String, String> properties = HoconPropertySource.load("example-hocon.conf");
        ValidationResult result = validator.validate(properties);

        if (result.isValid()) {
            System.out.println("  All properties valid");
        } else {
            System.out.println("  Validation errors:");
            result.getErrors().forEach(e ->
                    System.out.println("    - " + e.getPropertyName() + ": " + e.getErrorMessage()));
        }
        System.out.println();
    }

    // ── Example 5: Load HOCON from inline string ────────────────────────────

    private static void example5_loadFromString() {
        System.out.println("=== Example 5: Load HOCON from an inline string ===\n");

        String hocon = "service {\n"
                + "  name = \"my-service\"\n"
                + "  port = 9090\n"
                + "  endpoints = [\"/health\", \"/api/v1\", \"/metrics\"]\n"
                + "  timeouts {\n"
                + "    read = 30s\n"
                + "    write = 10s\n"
                + "    idle = 120s\n"
                + "  }\n"
                + "}\n";

        Map<String, String> properties = HoconPropertySource.loadFromString(hocon);

        System.out.println("  Loaded " + properties.size() + " properties from inline HOCON:\n");
        properties.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("    " + e.getKey() + " = " + e.getValue()));
        System.out.println();
    }
}
