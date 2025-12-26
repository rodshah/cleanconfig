package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.validation.MultiPropertyValidationRule;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for ConditionalRequirementRules.
 */
public class ConditionalRequirementRulesTest {

    @Test
    public void ifThen_shouldPassWhenIfPropertyNotSet() {
        PropertyContext context = mockContext();

        MultiPropertyValidationRule rule = ConditionalRequirementRules.ifThen(
                "ssl.enabled", "ssl.certPath");

        ValidationResult result = rule.validate(
                new String[]{"ssl.enabled", "ssl.certPath"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void ifThen_shouldPassWhenIfPropertyIsEmpty() {
        PropertyContext context = mockContext();
        when(context.hasProperty("ssl.enabled")).thenReturn(true);
        when(context.getProperty("ssl.enabled")).thenReturn(Optional.of("  "));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.ifThen(
                "ssl.enabled", "ssl.certPath");

        ValidationResult result = rule.validate(
                new String[]{"ssl.enabled", "ssl.certPath"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void ifThen_shouldPassWhenBothSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("ssl.enabled")).thenReturn(true);
        when(context.getProperty("ssl.enabled")).thenReturn(Optional.of("true"));
        when(context.hasProperty("ssl.certPath")).thenReturn(true);
        when(context.getProperty("ssl.certPath")).thenReturn(Optional.of("/path/to/cert"));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.ifThen(
                "ssl.enabled", "ssl.certPath");

        ValidationResult result = rule.validate(
                new String[]{"ssl.enabled", "ssl.certPath"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void ifThen_shouldFailWhenIfSetButThenNot() {
        PropertyContext context = mockContext();
        when(context.hasProperty("ssl.enabled")).thenReturn(true);
        when(context.getProperty("ssl.enabled")).thenReturn(Optional.of("true"));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.ifThen(
                "ssl.enabled", "ssl.certPath");

        ValidationResult result = rule.validate(
                new String[]{"ssl.enabled", "ssl.certPath"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage()
                .contains("ssl.certPath is required when ssl.enabled is set"));
    }

    @Test
    public void ifThen_shouldFailWhenThenIsEmpty() {
        PropertyContext context = mockContext();
        when(context.hasProperty("ssl.enabled")).thenReturn(true);
        when(context.getProperty("ssl.enabled")).thenReturn(Optional.of("true"));
        when(context.hasProperty("ssl.certPath")).thenReturn(true);
        when(context.getProperty("ssl.certPath")).thenReturn(Optional.of("   "));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.ifThen(
                "ssl.enabled", "ssl.certPath");

        ValidationResult result = rule.validate(
                new String[]{"ssl.enabled", "ssl.certPath"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void ifThen_shouldThrowExceptionForNullParameters() {
        assertThrows(NullPointerException.class,
                () -> ConditionalRequirementRules.ifThen(null, "thenProp"));
        assertThrows(NullPointerException.class,
                () -> ConditionalRequirementRules.ifThen("ifProp", null));
    }

    @Test
    public void allOrNothing_shouldPassWhenAllSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("db.username")).thenReturn(true);
        when(context.getProperty("db.username")).thenReturn(Optional.of("admin"));
        when(context.hasProperty("db.password")).thenReturn(true);
        when(context.getProperty("db.password")).thenReturn(Optional.of("secret"));
        when(context.hasProperty("db.host")).thenReturn(true);
        when(context.getProperty("db.host")).thenReturn(Optional.of("localhost"));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password", "db.host");

        ValidationResult result = rule.validate(
                new String[]{"db.username", "db.password", "db.host"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void allOrNothing_shouldPassWhenNoneSet() {
        PropertyContext context = mockContext();

        MultiPropertyValidationRule rule = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password", "db.host");

        ValidationResult result = rule.validate(
                new String[]{"db.username", "db.password", "db.host"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void allOrNothing_shouldFailWhenSomeSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("db.username")).thenReturn(true);
        when(context.getProperty("db.username")).thenReturn(Optional.of("admin"));
        when(context.hasProperty("db.password")).thenReturn(true);
        when(context.getProperty("db.password")).thenReturn(Optional.of("secret"));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password", "db.host");

        ValidationResult result = rule.validate(
                new String[]{"db.username", "db.password", "db.host"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage()
                .contains("All of [db.username, db.password, db.host]"
                        + " must be set together, or none at all"));
    }

    @Test
    public void allOrNothing_shouldFailWhenOnlyOneSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("db.username")).thenReturn(true);
        when(context.getProperty("db.username")).thenReturn(Optional.of("admin"));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password", "db.host");

        ValidationResult result = rule.validate(
                new String[]{"db.username", "db.password", "db.host"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Present: [db.username]"));
        assertTrue(result.getErrors().get(0).getErrorMessage()
                .contains("Missing: [db.password, db.host]"));
    }

    @Test
    public void allOrNothing_shouldIgnoreEmptyValues() {
        PropertyContext context = mockContext();
        when(context.hasProperty("db.username")).thenReturn(true);
        when(context.getProperty("db.username")).thenReturn(Optional.of("admin"));
        when(context.hasProperty("db.password")).thenReturn(true);
        when(context.getProperty("db.password")).thenReturn(Optional.of("   "));

        MultiPropertyValidationRule rule = ConditionalRequirementRules.allOrNothing(
                "db.username", "db.password");

        ValidationResult result = rule.validate(
                new String[]{"db.username", "db.password"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void allOrNothing_shouldThrowExceptionForLessThanTwoProperties() {
        assertThrows(IllegalArgumentException.class,
                () -> ConditionalRequirementRules.allOrNothing("single"));
    }

    @Test
    public void allOrNothing_shouldThrowExceptionForNullParameters() {
        assertThrows(NullPointerException.class,
                () -> ConditionalRequirementRules.allOrNothing((String[]) null));
    }

    private PropertyContext mockContext() {
        return mock(PropertyContext.class);
    }
}
