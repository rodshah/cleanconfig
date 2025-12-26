package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.validation.MultiPropertyValidationRule;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for ExclusivityRules.
 */
public class ExclusivityRulesTest {

    @Test
    public void mutuallyExclusive_shouldPassWhenNoneSet() {
        PropertyContext context = mockContext();

        MultiPropertyValidationRule rule = ExclusivityRules.mutuallyExclusive(
                "auth.password", "auth.apiKey", "auth.certificate");

        ValidationResult result = rule.validate(
                new String[]{"auth.password", "auth.apiKey", "auth.certificate"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void mutuallyExclusive_shouldPassWhenOnlyOneSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("auth.password")).thenReturn(true);
        when(context.getProperty("auth.password")).thenReturn(Optional.of("secret"));

        MultiPropertyValidationRule rule = ExclusivityRules.mutuallyExclusive(
                "auth.password", "auth.apiKey", "auth.certificate");

        ValidationResult result = rule.validate(
                new String[]{"auth.password", "auth.apiKey", "auth.certificate"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void mutuallyExclusive_shouldFailWhenTwoSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("auth.password")).thenReturn(true);
        when(context.getProperty("auth.password")).thenReturn(Optional.of("secret"));
        when(context.hasProperty("auth.apiKey")).thenReturn(true);
        when(context.getProperty("auth.apiKey")).thenReturn(Optional.of("key123"));

        MultiPropertyValidationRule rule = ExclusivityRules.mutuallyExclusive(
                "auth.password", "auth.apiKey", "auth.certificate");

        ValidationResult result = rule.validate(
                new String[]{"auth.password", "auth.apiKey", "auth.certificate"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Only one of"));
    }

    @Test
    public void mutuallyExclusive_shouldFailWhenAllSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("auth.password")).thenReturn(true);
        when(context.getProperty("auth.password")).thenReturn(Optional.of("secret"));
        when(context.hasProperty("auth.apiKey")).thenReturn(true);
        when(context.getProperty("auth.apiKey")).thenReturn(Optional.of("key123"));
        when(context.hasProperty("auth.certificate")).thenReturn(true);
        when(context.getProperty("auth.certificate")).thenReturn(Optional.of("cert.pem"));

        MultiPropertyValidationRule rule = ExclusivityRules.mutuallyExclusive(
                "auth.password", "auth.apiKey", "auth.certificate");

        ValidationResult result = rule.validate(
                new String[]{"auth.password", "auth.apiKey", "auth.certificate"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void mutuallyExclusive_shouldIgnoreEmptyValues() {
        PropertyContext context = mockContext();
        when(context.hasProperty("auth.password")).thenReturn(true);
        when(context.getProperty("auth.password")).thenReturn(Optional.of("secret"));
        when(context.hasProperty("auth.apiKey")).thenReturn(true);
        when(context.getProperty("auth.apiKey")).thenReturn(Optional.of("   "));

        MultiPropertyValidationRule rule = ExclusivityRules.mutuallyExclusive(
                "auth.password", "auth.apiKey");

        ValidationResult result = rule.validate(
                new String[]{"auth.password", "auth.apiKey"}, context);

        assertTrue(result.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mutuallyExclusive_shouldThrowExceptionForLessThanTwoProperties() {
        ExclusivityRules.mutuallyExclusive("single");
    }

    @Test
    public void atLeastOneRequired_shouldPassWhenOneSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("contact.email")).thenReturn(true);
        when(context.getProperty("contact.email")).thenReturn(Optional.of("test@example.com"));

        MultiPropertyValidationRule rule = ExclusivityRules.atLeastOneRequired(
                "contact.email", "contact.phone", "contact.address");

        ValidationResult result = rule.validate(
                new String[]{"contact.email", "contact.phone", "contact.address"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void atLeastOneRequired_shouldPassWhenMultipleSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("contact.email")).thenReturn(true);
        when(context.getProperty("contact.email")).thenReturn(Optional.of("test@example.com"));
        when(context.hasProperty("contact.phone")).thenReturn(true);
        when(context.getProperty("contact.phone")).thenReturn(Optional.of("555-1234"));

        MultiPropertyValidationRule rule = ExclusivityRules.atLeastOneRequired(
                "contact.email", "contact.phone", "contact.address");

        ValidationResult result = rule.validate(
                new String[]{"contact.email", "contact.phone", "contact.address"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void atLeastOneRequired_shouldFailWhenNoneSet() {
        PropertyContext context = mockContext();

        MultiPropertyValidationRule rule = ExclusivityRules.atLeastOneRequired(
                "contact.email", "contact.phone", "contact.address");

        ValidationResult result = rule.validate(
                new String[]{"contact.email", "contact.phone", "contact.address"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("At least one of"));
    }

    @Test
    public void atLeastOneRequired_shouldIgnoreEmptyValues() {
        PropertyContext context = mockContext();
        when(context.hasProperty("contact.email")).thenReturn(true);
        when(context.getProperty("contact.email")).thenReturn(Optional.of("  "));

        MultiPropertyValidationRule rule = ExclusivityRules.atLeastOneRequired(
                "contact.email", "contact.phone");

        ValidationResult result = rule.validate(
                new String[]{"contact.email", "contact.phone"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void exactlyOneRequired_shouldPassWhenExactlyOneSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("deploy.rolling")).thenReturn(true);
        when(context.getProperty("deploy.rolling")).thenReturn(Optional.of("true"));

        MultiPropertyValidationRule rule = ExclusivityRules.exactlyOneRequired(
                "deploy.rolling", "deploy.blueGreen", "deploy.canary");

        ValidationResult result = rule.validate(
                new String[]{"deploy.rolling", "deploy.blueGreen", "deploy.canary"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void exactlyOneRequired_shouldFailWhenNoneSet() {
        PropertyContext context = mockContext();

        MultiPropertyValidationRule rule = ExclusivityRules.exactlyOneRequired(
                "deploy.rolling", "deploy.blueGreen", "deploy.canary");

        ValidationResult result = rule.validate(
                new String[]{"deploy.rolling", "deploy.blueGreen", "deploy.canary"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("At least one of"));
    }

    @Test
    public void exactlyOneRequired_shouldFailWhenMultipleSet() {
        PropertyContext context = mockContext();
        when(context.hasProperty("deploy.rolling")).thenReturn(true);
        when(context.getProperty("deploy.rolling")).thenReturn(Optional.of("true"));
        when(context.hasProperty("deploy.blueGreen")).thenReturn(true);
        when(context.getProperty("deploy.blueGreen")).thenReturn(Optional.of("true"));

        MultiPropertyValidationRule rule = ExclusivityRules.exactlyOneRequired(
                "deploy.rolling", "deploy.blueGreen", "deploy.canary");

        ValidationResult result = rule.validate(
                new String[]{"deploy.rolling", "deploy.blueGreen", "deploy.canary"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Only one of"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exactlyOneRequired_shouldThrowExceptionForLessThanTwoProperties() {
        ExclusivityRules.exactlyOneRequired("single");
    }

    private PropertyContext mockContext() {
        return mock(PropertyContext.class);
    }
}
