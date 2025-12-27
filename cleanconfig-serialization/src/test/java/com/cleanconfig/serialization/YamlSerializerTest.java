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
 * Tests for YamlSerializer.
 */
public class YamlSerializerTest {

    private YamlSerializer serializer;
    private PropertyRegistry registry;
    private Map<String, String> properties;

    @Before
    public void setUp() throws Exception {
        serializer = new YamlSerializer();

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
    public void serialize_shouldProduceValidYaml() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.defaults());

        assertNotNull(result);
        assertTrue(result.contains("properties:"));
        assertTrue(result.contains("db.host:"));
        assertTrue(result.contains("prod-db.example.com"));
        assertTrue(result.contains("db.port:"));
        assertTrue(result.contains("3306") || result.contains("'3306'"));
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

        assertTrue(result.contains("metadata:"));
        assertTrue(result.contains("type:"));
        assertTrue(result.contains("String"));
        assertTrue(result.contains("Integer"));
        assertTrue(result.contains("category:"));
        assertTrue(result.contains("DATABASE"));
    }

    @Test
    public void serialize_withDescriptions_shouldIncludeDescriptions() throws Exception {
        SerializationOptions options = SerializationOptions.builder()
                .includeDescriptions(true)
                .build();

        String result = serializer.serialize(properties, registry, options);

        assertTrue("Result should contain 'metadata:', got: " + result, result.contains("metadata:"));
        assertTrue("Result should contain 'description:', got: " + result, result.contains("description:"));
        assertTrue("Result should contain 'Database host', got: " + result,
                result.contains("Database host"));
        assertTrue("Result should contain 'Database port', got: " + result,
                result.contains("Database port"));
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

        assertTrue("Result should contain 'db.host:', got: " + result, result.contains("db.host:"));
        assertTrue("Result should contain 'prod-db.example.com', got: " + result,
                result.contains("prod-db.example.com"));
        assertTrue("Result should contain 'db.port:', got: " + result, result.contains("db.port:"));
        assertTrue("Result should contain '5432' or '\"5432\"', got: " + result,
                result.contains("5432") || result.contains("'5432'") || result.contains("\"5432\""));
    }

    @Test
    public void serialize_toOutputStream_shouldWork() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), outputStream);

        String result = outputStream.toString("UTF-8");
        assertTrue(result.contains("db.host:"));
        assertTrue(result.contains("prod-db.example.com"));
    }

    @Test
    public void serialize_toWriter_shouldWork() throws Exception {
        StringWriter writer = new StringWriter();
        serializer.serialize(properties, registry, SerializationOptions.defaults(), writer);

        String result = writer.toString();
        assertTrue(result.contains("db.host:"));
        assertTrue(result.contains("prod-db.example.com"));
    }

    @Test
    public void serialize_verbose_shouldIncludeAllMetadata() throws Exception {
        String result = serializer.serialize(properties, registry, SerializationOptions.verbose());

        assertTrue(result.contains("properties:"));
        assertTrue(result.contains("metadata:"));
        assertTrue(result.contains("type:"));
        assertTrue(result.contains("category:"));
        assertTrue(result.contains("description:"));
        assertTrue(result.contains("defaultValue:"));
    }

    @Test
    public void deserialize_withoutPropertiesKey_shouldTreatWholeMapAsProperties() throws Exception {
        String simpleYaml = "db.host: localhost\ndb.port: 5432";
        Map<String, String> result = serializer.deserialize(simpleYaml);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    public void deserialize_withPropertiesKey_shouldExtractPropertiesSection() throws Exception {
        String structuredYaml = "properties:\n  db.host: localhost\n  db.port: 5432\nmetadata: {}";
        Map<String, String> result = serializer.deserialize(structuredYaml);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    public void serialize_withComplexValues_shouldHandleCorrectly() throws Exception {
        Map<String, String> complexProps = new HashMap<>();
        complexProps.put("test.url", "http://example.com:8080/path");
        complexProps.put("test.multiline", "line1\nline2");

        PropertyDefinition<String> urlDef = PropertyDefinition.builder(String.class)
                .name("test.url")
                .build();

        PropertyDefinition<String> multilineDef = PropertyDefinition.builder(String.class)
                .name("test.multiline")
                .build();

        PropertyRegistry testRegistry = PropertyRegistry.builder()
                .register(urlDef)
                .register(multilineDef)
                .build();

        String result = serializer.serialize(complexProps, testRegistry, SerializationOptions.defaults());

        assertNotNull(result);
        // YAML should handle these values correctly
    }

    @Test
    public void getFormatName_shouldReturnYaml() {
        assertEquals("YAML", serializer.getFormatName());
    }
}
