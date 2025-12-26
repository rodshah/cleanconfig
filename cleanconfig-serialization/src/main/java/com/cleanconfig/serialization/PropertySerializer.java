package com.cleanconfig.serialization;

import com.cleanconfig.core.PropertyRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * Interface for serializing properties to various formats.
 * <p>
 * Implementations serialize property values along with optional metadata
 * from a {@link PropertyRegistry}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * PropertySerializer serializer = new JsonSerializer();
 * String json = serializer.serialize(properties, registry, options);
 * }</pre>
 *
 * @since 0.1.0
 */
public interface PropertySerializer {

    /**
     * Serializes properties to a string.
     *
     * @param properties the property values to serialize
     * @param registry the property registry containing metadata
     * @param options serialization options
     * @return the serialized string
     * @throws SerializationException if serialization fails
     */
    String serialize(Map<String, String> properties,
                     PropertyRegistry registry,
                     SerializationOptions options) throws SerializationException;

    /**
     * Serializes properties to an output stream.
     *
     * @param properties the property values to serialize
     * @param registry the property registry containing metadata
     * @param options serialization options
     * @param outputStream the output stream to write to
     * @throws SerializationException if serialization fails
     * @throws IOException if an I/O error occurs
     */
    void serialize(Map<String, String> properties,
                   PropertyRegistry registry,
                   SerializationOptions options,
                   OutputStream outputStream) throws SerializationException, IOException;

    /**
     * Serializes properties to a writer.
     *
     * @param properties the property values to serialize
     * @param registry the property registry containing metadata
     * @param options serialization options
     * @param writer the writer to write to
     * @throws SerializationException if serialization fails
     * @throws IOException if an I/O error occurs
     */
    void serialize(Map<String, String> properties,
                   PropertyRegistry registry,
                   SerializationOptions options,
                   Writer writer) throws SerializationException, IOException;

    /**
     * Deserializes properties from a string.
     *
     * @param content the serialized content
     * @return the deserialized property values
     * @throws SerializationException if deserialization fails
     */
    Map<String, String> deserialize(String content) throws SerializationException;

    /**
     * Returns the format name for this serializer (e.g., "JSON", "YAML", "Properties").
     *
     * @return the format name
     */
    String getFormatName();
}
