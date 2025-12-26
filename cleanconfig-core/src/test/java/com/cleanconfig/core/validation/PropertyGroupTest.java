package com.cleanconfig.core.validation;

import com.cleanconfig.core.validation.multiproperty.ConditionalRequirementRules;
import com.cleanconfig.core.validation.multiproperty.NumericRelationshipRules;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests for PropertyGroup.
 */
public class PropertyGroupTest {

    @Test
    public void builder_shouldCreateGroupWithProperties() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperty("db.host")
                .addProperty("db.port")
                .addProperty("db.username")
                .build();

        assertEquals("database", group.getName());
        assertEquals(3, group.getPropertyNames().size());
        assertTrue(group.getPropertyNames().contains("db.host"));
        assertTrue(group.getPropertyNames().contains("db.port"));
        assertTrue(group.getPropertyNames().contains("db.username"));
    }

    @Test
    public void builder_shouldCreateGroupWithPropertiesArray() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port", "db.username")
                .build();

        assertEquals(3, group.getPropertyNames().size());
    }

    @Test
    public void builder_shouldCreateGroupWithRules() {
        MultiPropertyValidationRule rule1 = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password");
        MultiPropertyValidationRule rule2 = NumericRelationshipRules.greaterThan(
                "db.port", "db.minPort", Integer.class);

        PropertyGroup group = PropertyGroup.builder("database")
                .addProperty("db.host")
                .addProperty("db.port")
                .addRule(rule1)
                .addRule(rule2)
                .build();

        assertEquals(2, group.getRules().size());
    }

    @Test
    public void builder_shouldCreateGroupWithDescription() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperty("db.host")
                .description("Database connection properties")
                .build();

        assertEquals("Database connection properties", group.getDescription());
    }

    @Test
    public void builder_shouldCreateGroupWithoutDescription() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperty("db.host")
                .build();

        assertNull(group.getDescription());
    }

    @Test
    public void builder_shouldThrowExceptionForNullName() {
        assertThrows(NullPointerException.class,
                () -> PropertyGroup.builder(null));
    }

    @Test
    public void builder_shouldThrowExceptionForEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> PropertyGroup.builder("   "));
    }

    @Test
    public void builder_shouldThrowExceptionForNullProperty() {
        PropertyGroup.Builder builder = PropertyGroup.builder("test");

        assertThrows(NullPointerException.class,
                () -> builder.addProperty(null));
    }

    @Test
    public void builder_shouldThrowExceptionForNullProperties() {
        PropertyGroup.Builder builder = PropertyGroup.builder("test");

        assertThrows(NullPointerException.class,
                () -> builder.addProperties((String[]) null));
    }

    @Test
    public void builder_shouldThrowExceptionForNullRule() {
        PropertyGroup.Builder builder = PropertyGroup.builder("test")
                .addProperty("prop1");

        assertThrows(NullPointerException.class,
                () -> builder.addRule(null));
    }

    @Test
    public void builder_shouldThrowExceptionWhenNoPropertiesAdded() {
        PropertyGroup.Builder builder = PropertyGroup.builder("test");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void getPropertyNames_shouldReturnUnmodifiableList() {
        PropertyGroup group = PropertyGroup.builder("test")
                .addProperty("prop1")
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> group.getPropertyNames().add("prop2"));
    }

    @Test
    public void getRules_shouldReturnUnmodifiableList() {
        PropertyGroup group = PropertyGroup.builder("test")
                .addProperty("prop1")
                .addRule(MultiPropertyValidationRule.alwaysValid())
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> group.getRules().add(MultiPropertyValidationRule.alwaysValid()));
    }

    @Test
    public void equals_shouldReturnTrueForSameGroups() {
        PropertyGroup group1 = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port")
                .build();

        PropertyGroup group2 = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port")
                .build();

        assertEquals(group1, group2);
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    @Test
    public void toString_shouldIncludeGroupInfo() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port")
                .description("Database properties")
                .build();

        String str = group.toString();
        assertTrue(str.contains("database"));
        assertTrue(str.contains("propertyCount=2"));
        assertTrue(str.contains("Database properties"));
    }

    @Test
    public void toString_shouldNotIncludeDescriptionWhenNull() {
        PropertyGroup group = PropertyGroup.builder("database")
                .addProperty("db.host")
                .build();

        String str = group.toString();
        assertTrue(str.contains("database"));
        assertTrue(str.contains("propertyCount=1"));
    }

    @Test
    public void complexGroup_shouldWorkCorrectly() {
        // Create a comprehensive database configuration group
        PropertyGroup dbGroup = PropertyGroup.builder("database")
                .addProperties("db.host", "db.port", "db.username", "db.password")
                .addRule(ConditionalRequirementRules.allOrNothing("db.username", "db.password"))
                .addRule(NumericRelationshipRules.greaterThan("db.port", "db.minPort", Integer.class))
                .description("Database connection configuration")
                .build();

        assertNotNull(dbGroup);
        assertEquals("database", dbGroup.getName());
        assertEquals(4, dbGroup.getPropertyNames().size());
        assertEquals(2, dbGroup.getRules().size());
        assertEquals("Database connection configuration", dbGroup.getDescription());
    }
}
