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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for PropertiesSerializer.
 */
public class PropertiesSerializerTest {

    private PropertiesSerializer serializer;
    private PropertyRegistry registry;
    private Map<String, String> properties;

    @Before
    public void setUp() {
        serializer = new PropertiesSerializer();

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
    public void serialize_shouldProduceValidPropertiesFormat() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.defaults());

        assertNotNull(result);
        assertTrue(result.contains("db.host=prod-db.example.com"));
        assertTrue(result.contains("db.port=3306"));
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
    public void serialize_withMetadata_shouldIncludeComments() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .includeMetadata(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("# Type: String"));
        assertTrue(result.contains("# Type: Integer"));
        assertTrue(result.contains("# Category: DATABASE"));
    }

    @Test
    public void serialize_withDescriptions_shouldIncludeDescriptionComments() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .includeDescriptions(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("# Database host"));
        assertTrue(result.contains("# Database port"));
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

        assertTrue(result.contains("db.host=prod-db.example.com"));
        assertTrue(result.contains("db.port=5432"));
    }

    @Test
    public void serialize_compact_shouldHaveMinimalWhitespace() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.compact());

        assertFalse(result.contains("\n\n"));
    }

    @Test
    public void serialize_prettyPrint_shouldHaveExtraWhitespace() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .prettyPrint(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue(result.contains("\n\n"));
    }

    @Test
    public void serialize_toOutputStream_shouldWork() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), outputStream);

        String result = outputStream.toString("UTF-8");
        assertTrue(result.contains("db.host=prod-db.example.com"));
        assertTrue(result.contains("db.port=3306"));
    }

    @Test
    public void serialize_toWriter_shouldWork() throws Exception {
        StringWriter writer = new StringWriter();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), writer);

        String result = writer.toString();
        assertTrue(result.contains("db.host=prod-db.example.com"));
        assertTrue(result.contains("db.port=3306"));
    }

    @Test
    public void serialize_withSpecialCharacters_shouldEscapeProperly() throws Exception {
        Map<String, String> specialProps = new HashMap<>();
        specialProps.put("test.key", "value\nwith\nnewlines");

        PropertyDefinition<String> testDef = PropertyDefinition.builder(String.class)
                .name("test.key")
                .build();

        PropertyRegistry testRegistry = PropertyRegistry.builder()
                .register(testDef)
                .build();

        String result = serializer.serialize(specialProps, testRegistry, SerializationOptions.defaults());

        assertTrue(result.contains("\\n"));
        assertFalse(result.contains("value\nwith\nnewlines"));
    }

    @Test
    public void deserialize_shouldHandleEmptyProperties() throws Exception {
        String emptyProps = "";
        Map<String, String> result = serializer.deserialize(emptyProps);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void deserialize_shouldIgnoreComments() throws Exception {
        String propsWithComments = "# This is a comment\ndb.host=localhost\n# Another comment\ndb.port=5432";
        Map<String, String> result = serializer.deserialize(propsWithComments);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    public void getFormatName_shouldReturnProperties() {
        assertEquals("Properties", serializer.getFormatName());
    }
}
