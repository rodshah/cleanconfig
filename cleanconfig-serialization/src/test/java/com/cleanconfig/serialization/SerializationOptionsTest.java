package com.cleanconfig.serialization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for SerializationOptions.
 */
public class SerializationOptionsTest {

    @Test
    public void builder_shouldCreateOptionsWithDefaults() {
        SerializationOptions options = SerializationOptions.builder().build();

        assertNotNull(options);
        assertTrue(options.isPrettyPrint());
        assertFalse(options.isIncludeMetadata());
        assertFalse(options.isIncludeDefaults());
        assertFalse(options.isIncludeDescriptions());
    }

    @Test
    public void builder_shouldCreateOptionsWithCustomValues() {
        SerializationOptions options = SerializationOptions.builder()
                .prettyPrint(false)
                .includeMetadata(true)
                .includeDefaults(true)
                .includeDescriptions(true)
                .build();

        assertFalse(options.isPrettyPrint());
        assertTrue(options.isIncludeMetadata());
        assertTrue(options.isIncludeDefaults());
        assertTrue(options.isIncludeDescriptions());
    }

    @Test
    public void defaults_shouldReturnDefaultOptions() {
        SerializationOptions options = SerializationOptions.defaults();

        assertTrue(options.isPrettyPrint());
        assertFalse(options.isIncludeMetadata());
        assertFalse(options.isIncludeDefaults());
        assertFalse(options.isIncludeDescriptions());
    }

    @Test
    public void compact_shouldReturnCompactOptions() {
        SerializationOptions options = SerializationOptions.compact();

        assertFalse(options.isPrettyPrint());
        assertFalse(options.isIncludeMetadata());
        assertFalse(options.isIncludeDefaults());
        assertFalse(options.isIncludeDescriptions());
    }

    @Test
    public void verbose_shouldReturnVerboseOptions() {
        SerializationOptions options = SerializationOptions.verbose();

        assertTrue(options.isPrettyPrint());
        assertTrue(options.isIncludeMetadata());
        assertTrue(options.isIncludeDefaults());
        assertTrue(options.isIncludeDescriptions());
    }

    @Test
    public void equals_shouldReturnTrueForSameOptions() {
        SerializationOptions options1 = SerializationOptions.builder()
                .prettyPrint(true)
                .includeMetadata(true)
                .build();

        SerializationOptions options2 = SerializationOptions.builder()
                .prettyPrint(true)
                .includeMetadata(true)
                .build();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    public void toString_shouldIncludeAllFields() {
        SerializationOptions options = SerializationOptions.verbose();
        String str = options.toString();

        assertTrue(str.contains("prettyPrint=true"));
        assertTrue(str.contains("includeMetadata=true"));
        assertTrue(str.contains("includeDefaults=true"));
        assertTrue(str.contains("includeDescriptions=true"));
    }
}
