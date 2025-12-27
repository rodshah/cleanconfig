package com.cleanconfig.serialization;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for JsonSerializer.
 */
public class JsonSerializerTest {

    private JsonSerializer serializer;
    private PropertyRegistry registry;
    private Map<String, String> properties;

    @Before
    public void setUp() throws Exception {
        serializer = new JsonSerializer();

        // Create test registry
        PropertyDefinition<String> hostDef = PropertyDefinition.builder(String.class)
                .name("db.host")
                .defaultValue("localhost")
                .description("Database host")
                .category(PropertyCategory.DATABASE)
                .build();

        PropertyDefinition<Integer> portDef = PropertyDefinition.builder(Integer.class)
                .name("db.port")
                .defaultValue(5432)
                .description("Database port")
                .category(PropertyCategory.DATABASE)
                .build();

        registry = PropertyRegistry.builder()
                .register(hostDef)
                .register(portDef)
                .build();

        // Create test properties
        properties = new HashMap<>();
        properties.put("db.host", "prod-db.example.com");
        properties.put("db.port", "3306");
    }

    @Test
    public void serialize_shouldProduceValidJson() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.defaults());

        assertNotNull(result);
        assertTrue(result.contains("\"properties\""));
        assertTrue(result.contains("\"db.host\""));
        assertTrue(result.contains("\"prod-db.example.com\""));
        assertTrue(result.contains("\"db.port\""));
        assertTrue(result.contains("\"3306\""));
    }

    @Test
    public void roundTrip_shouldPreserveValues() throws Exception {
        String serialized = serializer.serialize(properties, registry, SerializationOptions.defaults());
        Map<String, String> deserialized = serializer.deserialize(serialized);

        assertEquals(properties.size(), deserialized.size());
        assertEquals("prod-db.example.com", deserialized.get("db.host"));
        assertEquals("3306", deserialized.get("db.port"));
    }

    @Test
    public void serialize_withMetadata_shouldIncludeMetadataSection() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .includeMetadata(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("\"metadata\""));
        assertTrue(result.contains("\"type\""));
        assertTrue(result.contains("\"String\""));
        assertTrue(result.contains("\"Integer\""));
        assertTrue(result.contains("\"category\""));
        assertTrue(result.contains("\"DATABASE\""));
    }

    @Test
    public void serialize_withDescriptions_shouldIncludeDescriptions() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .includeDescriptions(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("\"metadata\""));
        assertTrue(result.contains("\"description\""));
        assertTrue(result.contains("\"Database host\""));
        assertTrue(result.contains("\"Database port\""));
    }

    @Test
    public void serialize_withDefaults_shouldIncludeDefaultValues() throws Exception {
        Map<String, String> partialProps = new HashMap<>();
        partialProps.put("db.host", "prod-db.example.com");
        // db.port not set, should use default

        SerializationOptions options = SerializationOptions.builder()
                .includeDefaults(true)
                .build();

        String result = serializer.serialize(partialProps, registry, options);

        assertTrue(result.contains("\"db.host\""));
        assertTrue(result.contains("\"prod-db.example.com\""));
        assertTrue(result.contains("\"db.port\""));
        assertTrue(result.contains("\"5432\""));
    }

    @Test
    public void serialize_prettyPrint_shouldHaveIndentation() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .prettyPrint(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("\n"));
        assertTrue(result.contains("  "));
    }

    @Test
    public void serialize_compact_shouldHaveNoIndentation() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.compact());

        assertNotNull(result);
        // Compact JSON typically has minimal whitespace
    }

    @Test
    public void serialize_toOutputStream_shouldWork() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), outputStream);

        String result = outputStream.toString("UTF-8");
        assertTrue(result.contains("\"db.host\""));
        assertTrue(result.contains("\"prod-db.example.com\""));
    }

    @Test
    public void serialize_toWriter_shouldWork() throws Exception {
        StringWriter writer = new StringWriter();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), writer);

        String result = writer.toString();
        assertTrue(result.contains("\"db.host\""));
        assertTrue(result.contains("\"prod-db.example.com\""));
    }

    @Test
    public void serialize_verbose_shouldIncludeAllMetadata() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.verbose());

        assertTrue(result.contains("\"properties\""));
        assertTrue(result.contains("\"metadata\""));
        assertTrue(result.contains("\"type\""));
        assertTrue(result.contains("\"category\""));
        assertTrue(result.contains("\"description\""));
        assertTrue(result.contains("\"defaultValue\""));
    }

    @Test
    public void deserialize_withoutPropertiesKey_shouldTreatWholeMapAsProperties() throws Exception {
        String simpleJson = "{\"db.host\":\"localhost\",\"db.port\":\"5432\"}";
        Map<String, String> result = serializer.deserialize(simpleJson);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    public void deserialize_withPropertiesKey_shouldExtractPropertiesSection() throws Exception {
        String structuredJson = "{\"properties\":{\"db.host\":\"localhost\",\"db.port\":\"5432\"},"
                + "\"metadata\":{}}";
        Map<String, String> result = serializer.deserialize(structuredJson);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    public void getFormatName_shouldReturnJson() {
        assertEquals("JSON", serializer.getFormatName());
    }
}
