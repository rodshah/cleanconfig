package com.cleanconfig.serialization;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import com.cleanconfig.core.impl.DefaultPropertyContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Serializer for Java Properties format.
 * <p>
 * This serializer uses the standard {@link Properties} class to serialize
 * and deserialize property values. It supports optional metadata as comments.
 * </p>
 *
 * <p>Example output:</p>
 * <pre>
 * # Database Configuration
 * # Type: String
 * db.host=localhost
 * # Type: Integer
 * db.port=5432
 * </pre>
 *
 * @since 0.1.0
 */
public class PropertiesSerializer implements PropertySerializer {

    @Override
    public String serialize(Map<String, String> properties,
                            PropertyRegistry registry,
                            SerializationOptions options) throws SerializationException {
        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(registry, "Registry cannot be null");
        Objects.requireNonNull(options, "Options cannot be null");

        try {
            StringWriter writer = new StringWriter();
            serialize(properties, registry, options, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize properties", e);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          OutputStream outputStream) throws SerializationException, IOException {
        Objects.requireNonNull(outputStream, "Output stream cannot be null");

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            serialize(properties, registry, options, writer);
        }
    }

    @Override
    public void serialize(Map<String, String> properties,
                          PropertyRegistry registry,
                          SerializationOptions options,
                          Writer writer) throws SerializationException, IOException {
        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(registry, "Registry cannot be null");
        Objects.requireNonNull(options, "Options cannot be null");
        Objects.requireNonNull(writer, "Writer cannot be null");

        try {
            // Add properties to serialize
            Map<String, String> toSerialize = new HashMap<>(properties);

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

            // Build properties with optional metadata as comments
            StringBuilder output = new StringBuilder();

            // Write properties
            for (Map.Entry<String, String> entry : toSerialize.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Add metadata as comments
                if (options.isIncludeMetadata() || options.isIncludeDescriptions()) {
                    registry.getProperty(key).ifPresent(def -> {
                        if (options.isIncludeDescriptions()) {
                            def.getDescription().ifPresent(desc ->
                                    output.append("# ").append(desc).append("\n")
                            );
                        }
                        if (options.isIncludeMetadata()) {
                            output.append("# Type: ")
                                    .append(def.getType().getSimpleName())
                                    .append("\n");
                            if (def.getCategory() != null) {
                                output.append("# Category: ")
                                        .append(def.getCategory())
                                        .append("\n");
                            }
                        }
                    });
                }

                // Escape the value properly for properties format
                String escapedValue = escapePropertiesValue(value);
                output.append(key).append("=").append(escapedValue).append("\n");

                if (options.isPrettyPrint()) {
                    output.append("\n");
                }
            }

            writer.write(output.toString());
            writer.flush();

        } catch (Exception e) {
            throw new SerializationException("Failed to serialize properties", e);
        }
    }

    @Override
    public Map<String, String> deserialize(String content) throws SerializationException {
        Objects.requireNonNull(content, "Content cannot be null");

        try {
            Properties props = new Properties();
            props.load(new StringReader(content));

            Map<String, String> result = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            return result;

        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize properties", e);
        }
    }

    @Override
    public String getFormatName() {
        return "Properties";
    }

    /**
     * Escapes special characters in property values according to the Properties format.
     *
     * @param value the value to escape
     * @return the escaped value
     */
    private String escapePropertiesValue(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
