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
 * Serializer for YAML format using Jackson YAML.
 * <p>
 * This serializer requires Jackson YAML to be on the classpath. If Jackson YAML is not available,
 * a {@link SerializationException} will be thrown at runtime.
 * </p>
 *
 * <p>Example output (with metadata):</p>
 * <pre>
 * properties:
 *   db.host: localhost
 *   db.port: '5432'
 * metadata:
 *   db.host:
 *     type: String
 *     category: DATABASE
 *   db.port:
 *     type: Integer
 *     category: DATABASE
 * </pre>
 *
 * @since 0.1.0
 */
public class YamlSerializer implements PropertySerializer {

    private final Object yamlMapper;

    /**
     * Creates a new YAML serializer.
     *
     * @throws SerializationException if Jackson YAML is not available
     */
    public YamlSerializer() throws SerializationException {
        try {
            // Try to load Jackson YAML classes
            Class<?> yamlMapperClass = Class.forName(
                    "com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
            yamlMapper = yamlMapperClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(
                    "Jackson YAML is not available on the classpath. "
                            + "Add com.fasterxml.jackson.dataformat:jackson-dataformat-yaml "
                            + "to your dependencies.", e);
        } catch (Exception e) {
            throw new SerializationException("Failed to initialize Jackson YAMLMapper", e);
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
            Map<String, Object> root = buildYamlStructure(properties, registry, options);

            // Use reflection to call yamlMapper.writeValueAsString()
            return (String) yamlMapper.getClass()
                    .getMethod("writeValueAsString", Object.class)
                    .invoke(yamlMapper, root);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize properties to YAML", e);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          OutputStream outputStream) throws SerializationException, IOException {
        Objects.requireNonNull(outputStream, "Output stream cannot be null");

        try {
            Map<String, Object> root = buildYamlStructure(properties, registry, options);

            // Use reflection to call yamlMapper.writeValue()
            yamlMapper.getClass()
                    .getMethod("writeValue", OutputStream.class, Object.class)
                    .invoke(yamlMapper, outputStream, root);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize properties to YAML", e);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          Writer writer) throws SerializationException, IOException {
        Objects.requireNonNull(writer, "Writer cannot be null");

        String yaml = serialize(properties, registry, options);
        writer.write(yaml);
        writer.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> deserialize(String content) throws SerializationException {
        Objects.requireNonNull(content, "Content cannot be null");

        try {
            // Use reflection to call yamlMapper.readValue()
            Class<?> typeRefClass = Class.forName("com.fasterxml.jackson.core.type.TypeReference");
            Object typeRef = new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { };

            Object result = yamlMapper.getClass()
                    .getMethod("readValue", String.class, typeRefClass)
                    .invoke(yamlMapper, content, typeRef);

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
            throw new SerializationException("Failed to deserialize YAML", e);
        }
    }

    @Override
    public String getFormatName() {
        return "YAML";
    }

    /**
     * Builds the YAML structure based on serialization options.
     */
    private Map<String, Object> buildYamlStructure(Map<String, String> properties,
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
