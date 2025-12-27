package com.cleanconfig.serialization;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import com.cleanconfig.core.impl.DefaultPropertyContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Serializer for JSON format using Jackson.
 * <p>
 * This serializer requires Jackson to be on the classpath. If Jackson is not available,
 * a {@link SerializationException} will be thrown at runtime.
 * </p>
 *
 * <p>Example output (with metadata):</p>
 * <pre>{@code
 * {
 *   "properties": {
 *     "db.host": "localhost",
 *     "db.port": "5432"
 *   },
 *   "metadata": {
 *     "db.host": {
 *       "type": "String",
 *       "category": "DATABASE"
 *     },
 *     "db.port": {
 *       "type": "Integer",
 *       "category": "DATABASE"
 *     }
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
public class JsonSerializer implements PropertySerializer {

    private final Object objectMapper;

    /**
     * Creates a new JSON serializer.
     *
     * @throws SerializationException if Jackson is not available
     */
    public JsonSerializer() throws SerializationException {
        try {
            // Try to load Jackson classes
            Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapper = mapperClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(
                    "Jackson is not available on the classpath. "
                            + "Add com.fasterxml.jackson.core:jackson-databind to your dependencies.", e);
        } catch (Exception e) {
            throw new SerializationException("Failed to initialize Jackson ObjectMapper", e);
        }
    }

    @Override
    public String serialize(Map<String, String> properties,
                            PropertyRegistry registry,
                            SerializationOptions options) throws SerializationException {
        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(registry, "Registry cannot be null");
        Objects.requireNonNull(options, "Options cannot be null");

        try {
            Map<String, Object> root = buildJsonStructure(properties, registry, options);

            // Use reflection to call objectMapper.writeValueAsString()
            if (options.isPrettyPrint()) {
                Object writer = objectMapper.getClass()
                        .getMethod("writerWithDefaultPrettyPrinter")
                        .invoke(objectMapper);
                return (String) writer.getClass()
                        .getMethod("writeValueAsString", Object.class)
                        .invoke(writer, root);
            } else {
                return (String) objectMapper.getClass()
                        .getMethod("writeValueAsString", Object.class)
                        .invoke(objectMapper, root);
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize properties to JSON", e);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          OutputStream outputStream) throws SerializationException, IOException {
        Objects.requireNonNull(outputStream, "Output stream cannot be null");

        try {
            Map<String, Object> root = buildJsonStructure(properties, registry, options);

            // Use reflection to call objectMapper.writeValue()
            if (options.isPrettyPrint()) {
                Object writer = objectMapper.getClass()
                        .getMethod("writerWithDefaultPrettyPrinter")
                        .invoke(objectMapper);
                writer.getClass()
                        .getMethod("writeValue", OutputStream.class, Object.class)
                        .invoke(writer, outputStream, root);
            } else {
                objectMapper.getClass()
                        .getMethod("writeValue", OutputStream.class, Object.class)
                        .invoke(objectMapper, outputStream, root);
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize properties to JSON", e);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          Writer writer) throws SerializationException, IOException {
        Objects.requireNonNull(writer, "Writer cannot be null");

        String json = serialize(properties, registry, options);
        writer.write(json);
        writer.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> deserialize(String content) throws SerializationException {
        Objects.requireNonNull(content, "Content cannot be null");

        try {
            // Use reflection to call objectMapper.readValue()
            Class<?> typeRefClass = Class.forName("com.fasterxml.jackson.core.type.TypeReference");
            Object typeRef = new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { };

            Object result = objectMapper.getClass()
                    .getMethod("readValue", String.class, typeRefClass)
                    .invoke(objectMapper, content, typeRef);

            Map<String, Object> map = (Map<String, Object>) result;

            // Extract properties from the root structure
            if (map.containsKey("properties")) {
                Map<String, Object> props = (Map<String, Object>) map.get("properties");
                Map<String, String> stringProps = new HashMap<>();
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    stringProps.put(entry.getKey(),
                            entry.getValue() != null ? entry.getValue().toString() : null);
                }
                return stringProps;
            } else {
                // If no "properties" key, treat the whole map as properties
                Map<String, String> stringProps = new HashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    stringProps.put(entry.getKey(),
                            entry.getValue() != null ? entry.getValue().toString() : null);
                }
                return stringProps;
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize JSON", e);
        }
    }

    @Override
    public String getFormatName() {
        return "JSON";
    }

    /**
     * Builds the JSON structure based on serialization options.
     */
    private Map<String, Object> buildJsonStructure(Map<String, String> properties,
                                                     PropertyRegistry registry,
                                                     SerializationOptions options) {
        Map<String, Object> root = new LinkedHashMap<>();

        // Add properties to serialize
        Map<String, String> toSerialize = new LinkedHashMap<>(properties);

        // Include default values if requested
        if (options.isIncludeDefaults()) {
            // Create context for computing defaults
            PropertyContext context = new DefaultPropertyContext(
                    properties,
                    TypeConverterRegistry.getInstance()
            );

            registry.getAllProperties().stream()
                    .filter(def -> !toSerialize.containsKey(def.getName()))
                    .forEach(def -> {
                        def.getDefaultValue().ifPresent(conditionalDefault -> {
                            conditionalDefault.computeDefault(context).ifPresent(value -> {
                                toSerialize.put(def.getName(), value.toString());
                            });
                        });
                    });
        }

        // Add properties
        root.put("properties", toSerialize);

        // Add metadata if requested
        if (options.isIncludeMetadata() || options.isIncludeDescriptions()) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            toSerialize.keySet().forEach(key ->
                    registry.getProperty(key).ifPresent(def -> {
                        Map<String, Object> propMetadata = new LinkedHashMap<>();

                        if (options.isIncludeMetadata()) {
                            propMetadata.put("type", def.getType().getSimpleName());
                            if (def.getCategory() != null) {
                                propMetadata.put("category", def.getCategory().toString());
                            }
                            if (def.getDefaultValue() != null) {
                                propMetadata.put("defaultValue", def.getDefaultValue().toString());
                            }
                        }

                        if (options.isIncludeDescriptions()) {
                            def.getDescription().ifPresent(desc ->
                                    propMetadata.put("description", desc)
                            );
                        }

                        if (!propMetadata.isEmpty()) {
                            metadata.put(key, propMetadata);
                        }
                    })
            );

            if (!metadata.isEmpty()) {
                root.put("metadata", metadata);
            }
        }

        return root;
    }
}
