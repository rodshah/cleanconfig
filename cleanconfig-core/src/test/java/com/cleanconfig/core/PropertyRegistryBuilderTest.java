package com.cleanconfig.core;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link PropertyRegistryBuilder}.
 */
public class PropertyRegistryBuilderTest {

    private PropertyRegistryBuilder builder;

    @Before
    public void setUp() {
        builder = new PropertyRegistryBuilder();
    }

    @Test
    public void register_ValidProperty_ReturnsBuilder() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("test.property")
                .build();

        PropertyRegistryBuilder result = builder.register(property);

        assertThat(result).isSameAs(builder);
    }

    @Test
    public void register_NullProperty_ThrowsException() {
        assertThatThrownBy(() -> builder.register(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property definition cannot be null");
    }

    @Test
    public void build_PropertyWithNullName_ThrowsException() {
        assertThatThrownBy(() -> PropertyDefinition.builder(String.class)
                .name(null)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Property name is required");
    }

    @Test
    public void register_DuplicatePropertyName_ThrowsException() {
        PropertyDefinition<String> property1 = PropertyDefinition.builder(String.class)
                .name("test.property")
                .build();
        PropertyDefinition<String> property2 = PropertyDefinition.builder(String.class)
                .name("test.property")
                .build();

        builder.register(property1);

        assertThatThrownBy(() -> builder.register(property2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property 'test.property' is already registered");
    }

    @Test
    public void build_EmptyRegistry_ReturnsValidRegistry() {
        PropertyRegistry registry = builder.build();

        assertThat(registry).isNotNull();
        assertThat(registry.getAllProperties()).isEmpty();
    }

    @Test
    public void build_WithProperties_ReturnsRegistryWithAllProperties() {
        PropertyDefinition<String> property1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .build();
        PropertyDefinition<Integer> property2 = PropertyDefinition.builder(Integer.class)
                .name("prop2")
                .build();

        PropertyRegistry registry = builder
                .register(property1)
                .register(property2)
                .build();

        assertThat(registry.getAllProperties()).hasSize(2);
        assertThat(registry.isDefined("prop1")).isTrue();
        assertThat(registry.isDefined("prop2")).isTrue();
    }

    @Test
    public void build_WithValidDependencies_Succeeds() {
        PropertyDefinition<String> baseProp = PropertyDefinition.builder(String.class)
                .name("base.property")
                .build();

        PropertyDefinition<String> dependentProp = PropertyDefinition.builder(String.class)
                .name("dependent.property")
                .dependsOnForValidation("base.property")
                .build();

        PropertyRegistry registry = builder
                .register(baseProp)
                .register(dependentProp)
                .build();

        assertThat(registry.getAllProperties()).hasSize(2);
    }

    @Test
    public void build_WithUndefinedDependency_ThrowsException() {
        PropertyDefinition<String> dependentProp = PropertyDefinition.builder(String.class)
                .name("dependent.property")
                .dependsOnForValidation("undefined.property")
                .build();

        builder.register(dependentProp);

        assertThatThrownBy(() -> builder.build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("depends on undefined property 'undefined.property'");
    }

    @Test
    public void build_WithCircularDependency_TwoProperties_ThrowsException() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .dependsOnForValidation("prop2")
                .build();

        PropertyDefinition<String> prop2 = PropertyDefinition.builder(String.class)
                .name("prop2")
                .dependsOnForValidation("prop1")
                .build();

        builder.register(prop1).register(prop2);

        assertThatThrownBy(() -> builder.build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Circular dependency detected");
    }

    @Test
    public void build_WithCircularDependency_ThreeProperties_ThrowsException() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .dependsOnForValidation("prop2")
                .build();

        PropertyDefinition<String> prop2 = PropertyDefinition.builder(String.class)
                .name("prop2")
                .dependsOnForValidation("prop3")
                .build();

        PropertyDefinition<String> prop3 = PropertyDefinition.builder(String.class)
                .name("prop3")
                .dependsOnForValidation("prop1")
                .build();

        builder.register(prop1).register(prop2).register(prop3);

        assertThatThrownBy(() -> builder.build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Circular dependency detected");
    }

    @Test
    public void build_WithSelfDependency_ThrowsException() {
        PropertyDefinition<String> prop = PropertyDefinition.builder(String.class)
                .name("self.property")
                .dependsOnForValidation("self.property")
                .build();

        builder.register(prop);

        assertThatThrownBy(() -> builder.build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Circular dependency detected");
    }

    @Test
    public void build_WithComplexDependencyChain_Succeeds() {
        // Create a dependency chain: A -> B -> C -> D
        PropertyDefinition<String> propD = PropertyDefinition.builder(String.class)
                .name("propD")
                .build();

        PropertyDefinition<String> propC = PropertyDefinition.builder(String.class)
                .name("propC")
                .dependsOnForValidation("propD")
                .build();

        PropertyDefinition<String> propB = PropertyDefinition.builder(String.class)
                .name("propB")
                .dependsOnForValidation("propC")
                .build();

        PropertyDefinition<String> propA = PropertyDefinition.builder(String.class)
                .name("propA")
                .dependsOnForValidation("propB")
                .build();

        PropertyRegistry registry = builder
                .register(propA)
                .register(propB)
                .register(propC)
                .register(propD)
                .build();

        assertThat(registry.getAllProperties()).hasSize(4);
    }

    @Test
    public void build_WithDiamondDependency_Succeeds() {
        // Create a diamond dependency: A -> B, A -> C, B -> D, C -> D
        PropertyDefinition<String> propD = PropertyDefinition.builder(String.class)
                .name("propD")
                .build();

        PropertyDefinition<String> propB = PropertyDefinition.builder(String.class)
                .name("propB")
                .dependsOnForValidation("propD")
                .build();

        PropertyDefinition<String> propC = PropertyDefinition.builder(String.class)
                .name("propC")
                .dependsOnForValidation("propD")
                .build();

        PropertyDefinition<String> propA = PropertyDefinition.builder(String.class)
                .name("propA")
                .dependsOnForValidation("propB", "propC")
                .build();

        PropertyRegistry registry = builder
                .register(propA)
                .register(propB)
                .register(propC)
                .register(propD)
                .build();

        assertThat(registry.getAllProperties()).hasSize(4);
    }

    @Test
    public void build_PreservesRegistrationOrder() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .build();
        PropertyDefinition<String> prop2 = PropertyDefinition.builder(String.class)
                .name("prop2")
                .build();
        PropertyDefinition<String> prop3 = PropertyDefinition.builder(String.class)
                .name("prop3")
                .build();

        PropertyRegistry registry = builder
                .register(prop1)
                .register(prop2)
                .register(prop3)
                .build();

        // LinkedHashMap should preserve order
        assertThat(registry.getAllPropertyNames())
                .containsExactly("prop1", "prop2", "prop3");
    }
}
