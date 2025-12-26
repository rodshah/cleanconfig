package com.cleanconfig.core.impl;

import com.cleanconfig.core.ConditionalDefaultValue;
import com.cleanconfig.core.DefaultApplicationInfo;
import com.cleanconfig.core.DefaultApplicationResult;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.ValidationContextType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultValueApplierImpl}.
 */
public class DefaultValueApplierImplTest {

    private PropertyRegistry registry;
    private DefaultValueApplierImpl applier;

    @Before
    public void setUp() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .defaultValue("default1")
                .build();

        PropertyDefinition<Integer> prop2 = PropertyDefinition.builder(Integer.class)
                .name("prop2")
                .defaultValue(42)
                .build();

        PropertyDefinition<String> prop3 = PropertyDefinition.builder(String.class)
                .name("prop3")
                .build(); // No default

        registry = PropertyRegistry.builder()
                .register(prop1)
                .register(prop2)
                .register(prop3)
                .build();

        applier = new DefaultValueApplierImpl(registry);
    }

    @Test
    public void constructor_NullRegistry_ThrowsException() {
        assertThatThrownBy(() -> new DefaultValueApplierImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property registry cannot be null");
    }

    @Test
    public void applyDefaults_NullUserProperties_ThrowsException() {
        assertThatThrownBy(() -> applier.applyDefaults(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("User properties cannot be null");
    }

    @Test
    public void applyDefaults_NullContextType_ThrowsException() {
        assertThatThrownBy(() -> applier.applyDefaults(new HashMap<>(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Context type cannot be null");
    }

    @Test
    public void applyDefaults_EmptyUserProperties_AppliesAllDefaults() {
        Map<String, String> userProperties = new HashMap<>();

        DefaultApplicationResult result = applier.applyDefaults(userProperties);

        // Check final properties
        assertThat(result.getPropertiesWithDefaults()).hasSize(2);
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop1", "default1");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop2", "42");
        assertThat(result.getPropertiesWithDefaults()).doesNotContainKey("prop3");

        // Check application info
        DefaultApplicationInfo info = result.getApplicationInfo();
        assertThat(info.getAppliedDefaultsCount()).isEqualTo(2);
        assertThat(info.wasDefaultApplied("prop1")).isTrue();
        assertThat(info.wasDefaultApplied("prop2")).isTrue();
        assertThat(info.wasDefaultApplied("prop3")).isFalse();
    }

    @Test
    public void applyDefaults_UserProvidesValue_DoesNotOverride() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("prop1", "user-value");

        DefaultApplicationResult result = applier.applyDefaults(userProperties);

        // User value should be preserved
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop1", "user-value");

        // Default should not be applied
        DefaultApplicationInfo info = result.getApplicationInfo();
        assertThat(info.wasDefaultApplied("prop1")).isFalse();
        assertThat(info.wasDefaultApplied("prop2")).isTrue(); // But prop2 gets default
    }

    @Test
    public void applyDefaults_NoDefaults_ReturnsOriginalProperties() {
        PropertyDefinition<String> prop = PropertyDefinition.builder(String.class)
                .name("no.default")
                .build();

        PropertyRegistry registryNoDefaults = PropertyRegistry.builder()
                .register(prop)
                .build();

        DefaultValueApplierImpl applierNoDefaults = new DefaultValueApplierImpl(registryNoDefaults);

        Map<String, String> userProperties = new HashMap<>();

        DefaultApplicationResult result = applierNoDefaults.applyDefaults(userProperties);

        assertThat(result.getPropertiesWithDefaults()).isEmpty();
        assertThat(result.getApplicationInfo().getAppliedDefaultsCount()).isEqualTo(0);
    }

    @Test
    public void applyDefaults_ConditionalDefault_EvaluatesCondition() {
        // Create property with conditional default
        PropertyDefinition<String> modeProperty = PropertyDefinition.builder(String.class)
                .name("mode")
                .defaultValue("development")
                .build();

        PropertyDefinition<Integer> portProperty = PropertyDefinition.builder(Integer.class)
                .name("port")
                .defaultValue(ConditionalDefaultValue.computed(ctx -> {
                    String mode = ctx.getProperty("mode").orElse("development");
                    return mode.equals("production") ? Optional.of(80) : Optional.of(8080);
                }))
                .build();

        PropertyRegistry conditionalRegistry = PropertyRegistry.builder()
                .register(modeProperty)
                .register(portProperty)
                .build();

        DefaultValueApplierImpl conditionalApplier = new DefaultValueApplierImpl(conditionalRegistry);

        // Test with default mode (development)
        Map<String, String> userProperties1 = new HashMap<>();
        DefaultApplicationResult result1 = conditionalApplier.applyDefaults(userProperties1);

        assertThat(result1.getPropertiesWithDefaults()).containsEntry("mode", "development");
        assertThat(result1.getPropertiesWithDefaults()).containsEntry("port", "8080");

        // Test with user-provided production mode
        Map<String, String> userProperties2 = new HashMap<>();
        userProperties2.put("mode", "production");
        DefaultApplicationResult result2 = conditionalApplier.applyDefaults(userProperties2);

        assertThat(result2.getPropertiesWithDefaults()).containsEntry("mode", "production");
        assertThat(result2.getPropertiesWithDefaults()).containsEntry("port", "80");
    }

    @Test
    public void applyDefaults_ConditionalDefaultReturnsEmpty_NoDefaultApplied() {
        PropertyDefinition<String> prop = PropertyDefinition.builder(String.class)
                .name("conditional.prop")
                .defaultValue(ConditionalDefaultValue.computed(ctx -> Optional.empty()))
                .build();

        PropertyRegistry conditionalRegistry = PropertyRegistry.builder()
                .register(prop)
                .build();

        DefaultValueApplierImpl conditionalApplier = new DefaultValueApplierImpl(conditionalRegistry);

        Map<String, String> userProperties = new HashMap<>();
        DefaultApplicationResult result = conditionalApplier.applyDefaults(userProperties);

        assertThat(result.getPropertiesWithDefaults()).doesNotContainKey("conditional.prop");
        assertThat(result.getApplicationInfo().wasDefaultApplied("conditional.prop")).isFalse();
    }

    @Test
    public void applyDefaults_OriginalMapUnchanged() {
        Map<String, String> originalProperties = new HashMap<>();
        originalProperties.put("prop3", "user-value");

        DefaultApplicationResult result = applier.applyDefaults(originalProperties);

        // Original map should be unchanged
        assertThat(originalProperties).hasSize(1);
        assertThat(originalProperties).containsEntry("prop3", "user-value");

        // Result should have defaults applied
        assertThat(result.getPropertiesWithDefaults()).hasSize(3);
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop1", "default1");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop2", "42");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop3", "user-value");
    }

    @Test
    public void applyDefaults_WithContextType_PassesCorrectContext() {
        Map<String, String> userProperties = new HashMap<>();

        DefaultApplicationResult result = applier.applyDefaults(
                userProperties,
                ValidationContextType.RUNTIME_OVERRIDE
        );

        // Should still apply defaults
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop1", "default1");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop2", "42");
    }

    @Test
    public void applyDefaults_MultiplePropertiesWithDefaults_AppliesAll() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("a").defaultValue("default-a").build();
        PropertyDefinition<String> prop2 = PropertyDefinition.builder(String.class)
                .name("b").defaultValue("default-b").build();
        PropertyDefinition<String> prop3 = PropertyDefinition.builder(String.class)
                .name("c").defaultValue("default-c").build();

        PropertyRegistry multiRegistry = PropertyRegistry.builder()
                .register(prop1)
                .register(prop2)
                .register(prop3)
                .build();

        DefaultValueApplierImpl multiApplier = new DefaultValueApplierImpl(multiRegistry);

        Map<String, String> userProperties = new HashMap<>();

        DefaultApplicationResult result = multiApplier.applyDefaults(userProperties);

        assertThat(result.getPropertiesWithDefaults()).hasSize(3);
        assertThat(result.getApplicationInfo().getAppliedDefaultsCount()).isEqualTo(3);
    }

    @Test
    public void applyDefaults_MixedUserAndDefaults_CombinesCorrectly() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("prop1", "user-value-1");
        userProperties.put("prop3", "user-value-3");

        DefaultApplicationResult result = applier.applyDefaults(userProperties);

        // Check final properties
        assertThat(result.getPropertiesWithDefaults()).hasSize(3);
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop1", "user-value-1");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop2", "42");
        assertThat(result.getPropertiesWithDefaults()).containsEntry("prop3", "user-value-3");

        // Check application info
        DefaultApplicationInfo info = result.getApplicationInfo();
        assertThat(info.getAppliedDefaultsCount()).isEqualTo(1);
        assertThat(info.wasDefaultApplied("prop1")).isFalse();
        assertThat(info.wasDefaultApplied("prop2")).isTrue();
        assertThat(info.wasDefaultApplied("prop3")).isFalse();
    }
}
