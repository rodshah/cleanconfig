package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.serialization.JsonSerializer;
import com.cleanconfig.serialization.PropertiesSerializer;
import com.cleanconfig.serialization.SerializationOptions;
import com.cleanconfig.serialization.YamlSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating property serialization in multiple formats.
 *
 * <p>This example shows how to serialize and deserialize configuration properties
 * in Properties, JSON, and YAML formats with optional metadata.
 */
public class SerializationExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Serialization Example ===\n");

        // Define properties
        PropertyDefinition<String> dbHost = PropertyDefinition.builder(String.class)
                .name("db.host")
                .description("Database host")
                .defaultValue("localhost")
                .category(PropertyCategory.DATABASE)
                .build();

        PropertyDefinition<Integer> dbPort = PropertyDefinition.builder(Integer.class)
                .name("db.port")
                .description("Database port")
                .defaultValue(5432)
                .category(PropertyCategory.DATABASE)
                .build();

        PropertyDefinition<String> dbName = PropertyDefinition.builder(String.class)
                .name("db.name")
                .description("Database name")
                .defaultValue("myapp")
                .category(PropertyCategory.DATABASE)
                .build();

        // Create registry
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(dbHost)
                .register(dbPort)
                .register(dbName)
                .build();

        // User properties
        Map<String, String> properties = new HashMap<>();
        properties.put("db.host", "prod-db.example.com");
        properties.put("db.port", "3306");
        properties.put("db.name", "production");

        // Example 1: Properties format (no dependencies)
        System.out.println("Example 1: Properties Format");
        System.out.println("-----------------------------");
        PropertiesSerializer propertiesSerializer = new PropertiesSerializer();
        String propsOutput = propertiesSerializer.serialize(
                properties,
                registry,
                SerializationOptions.defaults()
        );
        System.out.println(propsOutput);

        // Example 2: JSON format with metadata
        System.out.println("\nExample 2: JSON Format with Metadata");
        System.out.println("-------------------------------------");
        JsonSerializer jsonSerializer = new JsonSerializer();
        String json = jsonSerializer.serialize(
                properties,
                registry,
                SerializationOptions.builder()
                        .prettyPrint(true)
                        .includeMetadata(true)
                        .includeDescriptions(true)
                        .build()
        );
        System.out.println(json);

        // Example 3: YAML format with all options
        System.out.println("\nExample 3: YAML Format (Verbose)");
        System.out.println("---------------------------------");
        YamlSerializer yamlSerializer = new YamlSerializer();
        String yaml = yamlSerializer.serialize(
                properties,
                registry,
                SerializationOptions.verbose()
        );
        System.out.println(yaml);

        // Example 4: Round-trip serialization
        System.out.println("\nExample 4: Round-Trip Serialization");
        System.out.println("------------------------------------");
        String serialized = yamlSerializer.serialize(properties, registry, SerializationOptions.defaults());
        Map<String, String> deserialized = yamlSerializer.deserialize(serialized);

        boolean equal = properties.equals(deserialized);
        System.out.println("Original properties: " + properties);
        System.out.println("Deserialized properties: " + deserialized);
        System.out.println("Round-trip successful: " + (equal ? "✓" : "✗"));

        // Example 5: Including defaults
        System.out.println("\nExample 5: Including Default Values");
        System.out.println("------------------------------------");
        Map<String, String> partialProps = new HashMap<>();
        partialProps.put("db.host", "prod-db.example.com");
        // db.port and db.name will use defaults

        String yamlWithDefaults = yamlSerializer.serialize(
                partialProps,
                registry,
                SerializationOptions.builder()
                        .includeDefaults(true)
                        .build()
        );
        System.out.println("User provided only db.host:");
        System.out.println(yamlWithDefaults);
    }
}
