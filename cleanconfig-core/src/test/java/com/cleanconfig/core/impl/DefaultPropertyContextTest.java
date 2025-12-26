package com.cleanconfig.core.impl;

import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultPropertyContext}.
 */
public class DefaultPropertyContextTest {

    private Map<String, String> properties;
    private TypeConverterRegistry converterRegistry;

    @Before
    public void setUp() {
        properties = new HashMap<>();
        properties.put("string.property", "value");
        properties.put("int.property", "42");
        properties.put("boolean.property", "true");

        converterRegistry = TypeConverterRegistry.getInstance();
    }

    @Test
    public void constructor_NullProperties_ThrowsException() {
        assertThatThrownBy(() -> new DefaultPropertyContext(
                null,
                ValidationContextType.STARTUP,
                converterRegistry
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Properties cannot be null");
    }

    @Test
    public void constructor_NullContextType_ThrowsException() {
        assertThatThrownBy(() -> new DefaultPropertyContext(
                properties,
                null,
                converterRegistry
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Context type cannot be null");
    }

    @Test
    public void constructor_NullConverterRegistry_ThrowsException() {
        assertThatThrownBy(() -> new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Converter registry cannot be null");
    }

    @Test
    public void constructor_NullMetadata_ThrowsException() {
        assertThatThrownBy(() -> new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Metadata cannot be null");
    }

    @Test
    public void getProperty_ExistingProperty_ReturnsValue() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<String> result = context.getProperty("string.property");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("value");
    }

    @Test
    public void getProperty_NonExistingProperty_ReturnsEmpty() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<String> result = context.getProperty("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    public void getTypedProperty_StringType_ReturnsConvertedValue() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<String> result = context.getTypedProperty("string.property", String.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("value");
    }

    @Test
    public void getTypedProperty_IntegerType_ReturnsConvertedValue() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<Integer> result = context.getTypedProperty("int.property", Integer.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(42);
    }

    @Test
    public void getTypedProperty_BooleanType_ReturnsConvertedValue() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<Boolean> result = context.getTypedProperty("boolean.property", Boolean.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    @Test
    public void getTypedProperty_NonExistingProperty_ReturnsEmpty() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<Integer> result = context.getTypedProperty("nonexistent", Integer.class);

        assertThat(result).isEmpty();
    }

    @Test
    public void getTypedProperty_InvalidConversion_ReturnsEmpty() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Optional<Integer> result = context.getTypedProperty("string.property", Integer.class);

        assertThat(result).isEmpty();
    }

    @Test
    public void getAllProperties_ReturnsAllProperties() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Map<String, String> result = context.getAllProperties();

        assertThat(result).hasSize(3);
        assertThat(result).containsEntry("string.property", "value");
        assertThat(result).containsEntry("int.property", "42");
        assertThat(result).containsEntry("boolean.property", "true");
    }

    @Test
    public void getAllProperties_ReturnsImmutableMap() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        Map<String, String> result = context.getAllProperties();

        assertThatThrownBy(() -> result.put("new.property", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void getContextType_ReturnsCorrectType() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        assertThat(context.getContextType()).isEqualTo(ValidationContextType.STARTUP);
    }

    @Test
    public void getContextType_RuntimeOverride_ReturnsCorrectType() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.RUNTIME_OVERRIDE,
                converterRegistry
        );

        assertThat(context.getContextType()).isEqualTo(ValidationContextType.RUNTIME_OVERRIDE);
    }

    @Test
    public void getMetadata_ExistingKey_ReturnsValue() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "production");

        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry,
                metadata
        );

        Optional<String> result = context.getMetadata("environment");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("production");
    }

    @Test
    public void getMetadata_NonExistingKey_ReturnsEmpty() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry,
                Collections.emptyMap()
        );

        Optional<String> result = context.getMetadata("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    public void hasProperty_ExistingProperty_ReturnsTrue() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        assertThat(context.hasProperty("string.property")).isTrue();
    }

    @Test
    public void hasProperty_NonExistingProperty_ReturnsFalse() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry
        );

        assertThat(context.hasProperty("nonexistent")).isFalse();
    }

    @Test
    public void emptyProperties_GetProperty_ReturnsEmpty() {
        DefaultPropertyContext context = new DefaultPropertyContext(
                Collections.emptyMap(),
                ValidationContextType.STARTUP,
                converterRegistry
        );

        assertThat(context.getProperty("any.property")).isEmpty();
        assertThat(context.getAllProperties()).isEmpty();
        assertThat(context.hasProperty("any.property")).isFalse();
    }

    @Test
    public void multipleMetadata_AllAccessible() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "production");
        metadata.put("region", "us-west");
        metadata.put("datacenter", "dc1");

        DefaultPropertyContext context = new DefaultPropertyContext(
                properties,
                ValidationContextType.STARTUP,
                converterRegistry,
                metadata
        );

        assertThat(context.getMetadata("environment")).hasValue("production");
        assertThat(context.getMetadata("region")).hasValue("us-west");
        assertThat(context.getMetadata("datacenter")).hasValue("dc1");
    }
}
