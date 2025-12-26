package com.cleanconfig.core;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertyRegistry}.
 */
public class PropertyRegistryTest {

    private PropertyRegistry registry;
    private PropertyDefinition<String> stringProperty;
    private PropertyDefinition<Integer> intProperty;

    @Before
    public void setUp() {
        stringProperty = PropertyDefinition.builder(String.class)
                .name("test.string")
                .description("Test string property")
                .defaultValue("default")
                .build();

        intProperty = PropertyDefinition.builder(Integer.class)
                .name("test.int")
                .description("Test integer property")
                .defaultValue(42)
                .build();

        registry = PropertyRegistry.builder()
                .register(stringProperty)
                .register(intProperty)
                .build();
    }

    @Test
    public void getProperty_ExistingProperty_ReturnsProperty() {
        Optional<PropertyDefinition<?>> result = registry.getProperty("test.string");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(stringProperty);
    }

    @Test
    public void getProperty_NonExistingProperty_ReturnsEmpty() {
        Optional<PropertyDefinition<?>> result = registry.getProperty("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    public void getProperty_NullPropertyName_ReturnsEmpty() {
        Optional<PropertyDefinition<?>> result = registry.getProperty(null);

        assertThat(result).isEmpty();
    }

    @Test
    public void isDefined_ExistingProperty_ReturnsTrue() {
        boolean result = registry.isDefined("test.string");

        assertThat(result).isTrue();
    }

    @Test
    public void isDefined_NonExistingProperty_ReturnsFalse() {
        boolean result = registry.isDefined("nonexistent");

        assertThat(result).isFalse();
    }

    @Test
    public void isDefined_NullPropertyName_ReturnsFalse() {
        boolean result = registry.isDefined(null);

        assertThat(result).isFalse();
    }

    @Test
    public void getAllProperties_ReturnsAllRegisteredProperties() {
        Collection<PropertyDefinition<?>> properties = registry.getAllProperties();

        assertThat(properties).hasSize(2);
        assertThat(properties).containsExactlyInAnyOrder(stringProperty, intProperty);
    }

    @Test
    public void getAllProperties_ReturnsImmutableCollection() {
        Collection<PropertyDefinition<?>> properties = registry.getAllProperties();

        assertThat(properties).isInstanceOf(Collection.class);
        // Verify immutability by checking that modifications fail
        PropertyDefinition<String> newProp = PropertyDefinition.builder(String.class)
                .name("new.property")
                .build();

        try {
            properties.add(newProp);
            // If we reach here, the collection is mutable (should not happen)
            assertThat(false).isTrue();
        } catch (UnsupportedOperationException e) {
            // Expected - collection is immutable
            assertThat(true).isTrue();
        }
    }

    @Test
    public void getAllPropertyNames_ReturnsAllNames() {
        Collection<String> names = registry.getAllPropertyNames();

        assertThat(names).hasSize(2);
        assertThat(names).containsExactlyInAnyOrder("test.string", "test.int");
    }

    @Test
    public void getAllPropertyNames_ReturnsImmutableCollection() {
        Collection<String> names = registry.getAllPropertyNames();

        try {
            names.add("new.property");
            // If we reach here, the collection is mutable (should not happen)
            assertThat(false).isTrue();
        } catch (UnsupportedOperationException e) {
            // Expected - collection is immutable
            assertThat(true).isTrue();
        }
    }

    @Test
    public void builder_CreatesNewBuilder() {
        PropertyRegistryBuilder builder = PropertyRegistry.builder();

        assertThat(builder).isNotNull();
        assertThat(builder).isInstanceOf(PropertyRegistryBuilder.class);
    }

    @Test
    public void emptyRegistry_ReturnsEmptyCollections() {
        PropertyRegistry emptyRegistry = PropertyRegistry.builder().build();

        assertThat(emptyRegistry.getAllProperties()).isEmpty();
        assertThat(emptyRegistry.getAllPropertyNames()).isEmpty();
        assertThat(emptyRegistry.isDefined("any.property")).isFalse();
        assertThat(emptyRegistry.getProperty("any.property")).isEmpty();
    }
}
