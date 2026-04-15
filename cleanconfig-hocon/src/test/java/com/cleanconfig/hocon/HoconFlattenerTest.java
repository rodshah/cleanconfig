package com.cleanconfig.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link HoconFlattener}.
 */
class HoconFlattenerTest {

    @Test
    void flatten_flatKeyValuePairs_shouldReturnDirectMapping() {
        final String hocon = "host = \"localhost\"\nport = 8080";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("localhost", result.get("host"));
        assertEquals("8080", result.get("port"));
        assertEquals(2, result.size());
    }

    @Test
    void flatten_nestedObjects_shouldUseDotNotation() {
        final String hocon = "server { host = \"localhost\"\n port = 9090 }";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("localhost", result.get("server.host"));
        assertEquals("9090", result.get("server.port"));
    }

    @Test
    void flatten_arrays_shouldUseIndexedKeys() {
        final String hocon = "items = [\"a\", \"b\", \"c\"]";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("a", result.get("items.0"));
        assertEquals("b", result.get("items.1"));
        assertEquals("c", result.get("items.2"));
        assertEquals(3, result.size());
    }

    @Test
    void flatten_mixedNesting_objectInsideArray() {
        final String hocon = "servers = [{ host = \"a\", port = 1 }, { host = \"b\", port = 2 }]";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("a", result.get("servers.0.host"));
        assertEquals("1", result.get("servers.0.port"));
        assertEquals("b", result.get("servers.1.host"));
        assertEquals("2", result.get("servers.1.port"));
    }

    @Test
    void flatten_mixedNesting_arrayInsideObject() {
        final String hocon = "config { tags = [\"alpha\", \"beta\"] }";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("alpha", result.get("config.tags.0"));
        assertEquals("beta", result.get("config.tags.1"));
    }

    @Test
    void flatten_nullValues_shouldBeOmitted() {
        final String hocon = "present = \"yes\"\nabsent = null";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("yes", result.get("present"));
        assertFalse(result.containsKey("absent"));
    }

    @Test
    void flatten_booleanValues_shouldConvertToString() {
        final String hocon = "enabled = true\ndisabled = false";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("true", result.get("enabled"));
        assertEquals("false", result.get("disabled"));
    }

    @Test
    void flatten_numericValues_shouldConvertToString() {
        final String hocon = "count = 42\nprice = 19.99";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("42", result.get("count"));
        assertEquals("19.99", result.get("price"));
    }

    @Test
    void flatten_emptyConfig_shouldReturnEmptyMap() {
        final Config config = ConfigFactory.empty();

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertTrue(result.isEmpty());
    }

    @Test
    void flatten_deeplyNestedConfig_shouldFlattenCorrectly() {
        final String hocon = "a { b { c { d { e { value = \"deep\" } } } } }";
        final Config config = ConfigFactory.parseString(hocon);

        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals("deep", result.get("a.b.c.d.e.value"));
        assertEquals(1, result.size());
    }

    @Test
    void flatten_largeArray_shouldFlattenAllElements() {
        final StringBuilder hocon = new StringBuilder("numbers = [");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                hocon.append(", ");
            }
            hocon.append(i);
        }
        hocon.append("]");

        final Config config = ConfigFactory.parseString(hocon.toString());
        final Map<String, String> result = HoconFlattener.flatten(config);

        assertEquals(100, result.size());
        assertEquals("0", result.get("numbers.0"));
        assertEquals("99", result.get("numbers.99"));
    }

    @Test
    void flatten_nullConfig_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> HoconFlattener.flatten(null));
    }

    @Test
    void flatten_resultShouldBeUnmodifiable() {
        final Config config = ConfigFactory.parseString("key = \"value\"");
        final Map<String, String> result = HoconFlattener.flatten(config);

        assertThrows(UnsupportedOperationException.class, () -> result.put("new", "entry"));
    }
}
