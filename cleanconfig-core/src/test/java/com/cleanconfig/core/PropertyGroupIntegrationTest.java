package com.cleanconfig.core;

import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.PropertyGroup;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.multiproperty.ConditionalRequirementRules;
import com.cleanconfig.core.validation.multiproperty.ExclusivityRules;
import com.cleanconfig.core.validation.multiproperty.NumericRelationshipRules;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for PropertyGroup with PropertyRegistry and PropertyValidator.
 */
public class PropertyGroupIntegrationTest {

    @Test
    public void propertyRegistry_shouldStoreAndRetrievePropertyGroups() {
        PropertyGroup dbGroup = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port")
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class).name("db.host").build())
                .register(PropertyDefinition.builder(Integer.class).name("db.port").build())
                .registerGroup(dbGroup)
                .build();

        assertTrue(registry.getPropertyGroup("database").isPresent());
        assertEquals(dbGroup, registry.getPropertyGroup("database").get());
        assertEquals(1, registry.getAllPropertyGroups().size());
    }

    @Test
    public void propertyRegistry_shouldThrowExceptionForDuplicateGroupName() {
        PropertyGroup group1 = PropertyGroup.builder("database")
                .addProperty("db.host")
                .build();

        PropertyGroup group2 = PropertyGroup.builder("database")
                .addProperty("db.port")
                .build();

        PropertyRegistryBuilder builder = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class).name("db.host").build())
                .register(PropertyDefinition.builder(Integer.class).name("db.port").build())
                .registerGroup(group1);

        assertThrows(IllegalArgumentException.class,
                () -> builder.registerGroup(group2));
    }

    @Test
    public void propertyValidator_shouldValidatePropertyGroups() {
        // Create a group with a rule: db.username and db.password must be set together
        PropertyGroup dbGroup = PropertyGroup.builder("database")
                .addProperties("db.username", "db.password")
                .addRule(ConditionalRequirementRules.allOrNothing("db.username", "db.password"))
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class).name("db.username").build())
                .register(PropertyDefinition.builder(String.class).name("db.password").build())
                .registerGroup(dbGroup)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Test with both set - should pass
        Map<String, String> properties1 = new HashMap<>();
        properties1.put("db.username", "admin");
        properties1.put("db.password", "secret");

        ValidationResult result1 = validator.validate(properties1);
        assertTrue(result1.isValid());

        // Test with only one set - should fail
        Map<String, String> properties2 = new HashMap<>();
        properties2.put("db.username", "admin");

        ValidationResult result2 = validator.validate(properties2);
        assertFalse(result2.isValid());
        assertTrue(result2.getErrors().get(0).getErrorMessage()
                .contains("must be set together, or none at all"));
    }

    @Test
    public void propertyValidator_shouldValidatePropertyGroupDirectly() {
        PropertyGroup dbGroup = PropertyGroup.builder("database")
                .addProperties("db.username", "db.password")
                .addRule(ConditionalRequirementRules.allOrNothing("db.username", "db.password"))
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class).name("db.username").build())
                .register(PropertyDefinition.builder(String.class).name("db.password").build())
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        Map<String, String> properties = new HashMap<>();
        properties.put("db.username", "admin");

        ValidationResult result = validator.validatePropertyGroup(dbGroup, properties);
        assertFalse(result.isValid());
    }

    @Test
    public void propertyValidator_shouldValidateMultipleGroups() {
        // Database group
        PropertyGroup dbGroup = PropertyGroup.builder("database")
                .addProperties("db.username", "db.password")
                .addRule(ConditionalRequirementRules.allOrNothing("db.username", "db.password"))
                .build();

        // Authentication group
        PropertyGroup authGroup = PropertyGroup.builder("authentication")
                .addProperties("auth.password", "auth.apiKey", "auth.certificate")
                .addRule(ExclusivityRules.exactlyOneRequired(
                        "auth.password", "auth.apiKey", "auth.certificate"))
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class).name("db.username").build())
                .register(PropertyDefinition.builder(String.class).name("db.password").build())
                .register(PropertyDefinition.builder(String.class).name("auth.password").build())
                .register(PropertyDefinition.builder(String.class).name("auth.apiKey").build())
                .register(PropertyDefinition.builder(String.class).name("auth.certificate").build())
                .registerGroup(dbGroup)
                .registerGroup(authGroup)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Valid configuration
        Map<String, String> validProps = new HashMap<>();
        validProps.put("db.username", "admin");
        validProps.put("db.password", "secret");
        validProps.put("auth.password", "password123");

        ValidationResult validResult = validator.validate(validProps);
        assertTrue(validResult.isValid());

        // Invalid configuration - missing db.password and multiple auth methods
        Map<String, String> invalidProps = new HashMap<>();
        invalidProps.put("db.username", "admin");
        invalidProps.put("auth.password", "password123");
        invalidProps.put("auth.apiKey", "key123");

        ValidationResult invalidResult = validator.validate(invalidProps);
        assertFalse(invalidResult.isValid());
        assertEquals(2, invalidResult.getErrors().size());
    }

    @Test
    public void propertyValidator_shouldValidateNumericRelationshipsInGroups() {
        PropertyGroup resourceGroup = PropertyGroup.builder("resources")
                .addProperties("cpu.request", "cpu.limit", "memory.request", "memory.limit")
                .addRule(NumericRelationshipRules.lessThanOrEqual(
                        "cpu.request", "cpu.limit", Integer.class))
                .addRule(NumericRelationshipRules.lessThanOrEqual(
                        "memory.request", "memory.limit", Long.class))
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(Integer.class).name("cpu.request").build())
                .register(PropertyDefinition.builder(Integer.class).name("cpu.limit").build())
                .register(PropertyDefinition.builder(Long.class).name("memory.request").build())
                .register(PropertyDefinition.builder(Long.class).name("memory.limit").build())
                .registerGroup(resourceGroup)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Valid configuration
        Map<String, String> validProps = new HashMap<>();
        validProps.put("cpu.request", "2");
        validProps.put("cpu.limit", "4");
        validProps.put("memory.request", "512");
        validProps.put("memory.limit", "1024");

        ValidationResult validResult = validator.validate(validProps);
        assertTrue(validResult.isValid());

        // Invalid configuration - request > limit
        Map<String, String> invalidProps = new HashMap<>();
        invalidProps.put("cpu.request", "8");
        invalidProps.put("cpu.limit", "4");
        invalidProps.put("memory.request", "512");
        invalidProps.put("memory.limit", "1024");

        ValidationResult invalidResult = validator.validate(invalidProps);
        assertFalse(invalidResult.isValid());
        assertTrue(invalidResult.getErrors().get(0).getErrorMessage()
                .contains("must be less than or equal to"));
    }

    @Test
    public void propertyValidator_shouldValidateComplexGroupWithMultipleRules() {
        // Kubernetes-style resource configuration group
        PropertyGroup k8sResourceGroup = PropertyGroup.builder("kubernetes-resources")
                .addProperties(
                        "resources.cpu.request",
                        "resources.cpu.limit",
                        "resources.memory.request",
                        "resources.memory.limit",
                        "resources.ephemeral.request",
                        "resources.ephemeral.limit"
                )
                .addRule(NumericRelationshipRules.lessThanOrEqual(
                        "resources.cpu.request", "resources.cpu.limit", Integer.class))
                .addRule(NumericRelationshipRules.lessThanOrEqual(
                        "resources.memory.request", "resources.memory.limit", Long.class))
                .addRule(NumericRelationshipRules.lessThanOrEqual(
                        "resources.ephemeral.request", "resources.ephemeral.limit", Long.class))
                .addRule(ConditionalRequirementRules.ifThen(
                        "resources.ephemeral.request", "resources.ephemeral.limit"))
                .description("Kubernetes resource constraints")
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(Integer.class).name("resources.cpu.request").build())
                .register(PropertyDefinition.builder(Integer.class).name("resources.cpu.limit").build())
                .register(PropertyDefinition.builder(Long.class).name("resources.memory.request").build())
                .register(PropertyDefinition.builder(Long.class).name("resources.memory.limit").build())
                .register(PropertyDefinition.builder(Long.class).name("resources.ephemeral.request").build())
                .register(PropertyDefinition.builder(Long.class).name("resources.ephemeral.limit").build())
                .registerGroup(k8sResourceGroup)
                .build();

        PropertyValidator validator = new DefaultPropertyValidator(registry);

        // Test valid configuration
        Map<String, String> validProps = new HashMap<>();
        validProps.put("resources.cpu.request", "2");
        validProps.put("resources.cpu.limit", "4");
        validProps.put("resources.memory.request", "512");
        validProps.put("resources.memory.limit", "1024");
        validProps.put("resources.ephemeral.request", "100");
        validProps.put("resources.ephemeral.limit", "200");

        ValidationResult validResult = validator.validate(validProps);
        assertTrue(validResult.isValid());

        // Test invalid: ephemeral request set but not limit
        Map<String, String> invalidProps = new HashMap<>();
        invalidProps.put("resources.cpu.request", "2");
        invalidProps.put("resources.cpu.limit", "4");
        invalidProps.put("resources.memory.request", "512");
        invalidProps.put("resources.memory.limit", "1024");
        invalidProps.put("resources.ephemeral.request", "100");

        ValidationResult invalidResult = validator.validate(invalidProps);
        assertFalse(invalidResult.isValid());
    }
}
