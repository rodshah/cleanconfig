package com.cleanconfig.hocon;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link HoconPropertySource}.
 */
class HoconPropertySourceTest {

    @Test
    void load_classpathResource_shouldReturnFlattenedConfig() {
        final Map<String, String> result = HoconPropertySource.load("test-config.conf");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("localhost", result.get("server.host"));
        assertEquals("8080", result.get("server.port"));
        assertEquals("jdbc:postgresql://localhost:5432/test", result.get("database.url"));
        assertEquals("10", result.get("database.pool-size"));
        assertEquals("test-app", result.get("app.name"));
        assertEquals("true", result.get("app.debug"));
    }

    @Test
    void load_classpathResource_shouldFlattenArrays() {
        final Map<String, String> result = HoconPropertySource.load("test-config.conf");

        assertEquals("auth", result.get("features.0"));
        assertEquals("logging", result.get("features.1"));
        assertEquals("metrics", result.get("features.2"));
    }

    @Test
    void load_classpathResource_shouldResolveSubstitutions() {
        final Map<String, String> result = HoconPropertySource.load("test-config.conf");

        assertEquals("localhost", result.get("resolved-value"));
    }

    @Test
    void loadFromString_shouldParseFlatConfig() {
        final String hocon = "host = \"myhost\"\nport = 3000";
        final Map<String, String> result = HoconPropertySource.loadFromString(hocon);

        assertEquals("myhost", result.get("host"));
        assertEquals("3000", result.get("port"));
    }

    @Test
    void loadFromString_shouldResolveSubstitutions() {
        final String hocon = "base = \"hello\"\nderived = ${base}";
        final Map<String, String> result = HoconPropertySource.loadFromString(hocon);

        assertEquals("hello", result.get("base"));
        assertEquals("hello", result.get("derived"));
    }

    @Test
    void loadFromString_nestedConfig_shouldFlatten() {
        final String hocon = "db { host = \"localhost\"\n port = 5432 }";
        final Map<String, String> result = HoconPropertySource.loadFromString(hocon);

        assertEquals("localhost", result.get("db.host"));
        assertEquals("5432", result.get("db.port"));
    }

    @Test
    void load_missingResource_shouldThrowHoconLoadException() {
        assertThrows(HoconLoadException.class,
                () -> HoconPropertySource.load("nonexistent-file.conf"));
    }

    @Test
    void loadFromString_nullContent_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> HoconPropertySource.loadFromString(null));
    }

    @Test
    void load_nullResourcePath_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> HoconPropertySource.load(null));
    }

    @Test
    void loadFromString_returnedMapShouldBeUnmodifiable() {
        final String hocon = "key = \"value\"";
        final Map<String, String> result = HoconPropertySource.loadFromString(hocon);

        assertThrows(UnsupportedOperationException.class, () -> result.put("new", "entry"));
    }

    @Test
    void load_classpathResource_returnedMapShouldBeUnmodifiable() {
        final Map<String, String> result = HoconPropertySource.load("test-config.conf");

        assertThrows(UnsupportedOperationException.class, () -> result.put("new", "entry"));
    }

    @Test
    void loadFromString_emptyString_shouldReturnEmptyMap() {
        final Map<String, String> result = HoconPropertySource.loadFromString("");

        assertTrue(result.isEmpty());
    }

    @Test
    void loadFromString_withDurationValue_shouldRenderAsString() {
        final String hocon = "timeout = 30s";
        final Map<String, String> result = HoconPropertySource.loadFromString(hocon);

        // Duration values are rendered as their HOCON string representation
        assertNotNull(result.get("timeout"));
        assertFalse(result.get("timeout").isEmpty());
    }
}
