package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for MultiPropertyValidationRule interface and its default methods.
 */
public class MultiPropertyValidationRuleTest {

    @Test
    public void and_shouldPassWhenBothRulesPass() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysValid();
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysValid();

        ValidationResult result = rule1.and(rule2).validate(new String[]{"prop"}, mockContext());

        assertTrue(result.isValid());
    }

    @Test
    public void and_shouldFailWhenFirstRuleFails() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysFails("First failed");
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysValid();

        ValidationResult result = rule1.and(rule2).validate(new String[]{"prop"}, mockContext());

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("First failed"));
    }

    @Test
    public void and_shouldFailWhenSecondRuleFails() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysValid();
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysFails("Second failed");

        ValidationResult result = rule1.and(rule2).validate(new String[]{"prop"}, mockContext());

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Second failed"));
    }

    @Test
    public void or_shouldPassWhenFirstRulePasses() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysValid();
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysFails("Should not see this");

        ValidationResult result = rule1.or(rule2).validate(new String[]{"prop"}, mockContext());

        assertTrue(result.isValid());
    }

    @Test
    public void or_shouldPassWhenSecondRulePasses() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysFails("First failed");
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysValid();

        ValidationResult result = rule1.or(rule2).validate(new String[]{"prop"}, mockContext());

        assertTrue(result.isValid());
    }

    @Test
    public void or_shouldFailWhenBothRulesFail() {
        MultiPropertyValidationRule rule1 = MultiPropertyValidationRule.alwaysFails("First failed");
        MultiPropertyValidationRule rule2 = MultiPropertyValidationRule.alwaysFails("Second failed");

        ValidationResult result = rule1.or(rule2).validate(new String[]{"prop"}, mockContext());

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Second failed"));
    }

    @Test
    public void onlyIf_shouldPassWhenConditionIsFalse() {
        PropertyContext context = mockContext();
        MultiPropertyValidationRule rule = MultiPropertyValidationRule
                .alwaysFails("Should not see this")
                .onlyIf(ctx -> false);

        ValidationResult result = rule.validate(new String[]{"prop"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void onlyIf_shouldValidateWhenConditionIsTrue() {
        PropertyContext context = mockContext();
        MultiPropertyValidationRule rule = MultiPropertyValidationRule
                .alwaysFails("Condition met")
                .onlyIf(ctx -> true);

        ValidationResult result = rule.validate(new String[]{"prop"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Condition met"));
    }

    @Test
    public void alwaysValid_shouldAlwaysPass() {
        ValidationResult result = MultiPropertyValidationRule.alwaysValid()
                .validate(new String[]{"prop"}, mockContext());

        assertTrue(result.isValid());
    }

    @Test
    public void alwaysFails_shouldAlwaysFail() {
        ValidationResult result = MultiPropertyValidationRule.alwaysFails("Test error")
                .validate(new String[]{"prop"}, mockContext());

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Test error"));
    }

    @Test
    public void complexComposition_shouldWorkCorrectly() {
        // (valid AND valid) OR (fails) = valid
        MultiPropertyValidationRule rule = MultiPropertyValidationRule.alwaysValid()
                .and(MultiPropertyValidationRule.alwaysValid())
                .or(MultiPropertyValidationRule.alwaysFails("Should not see this"));

        ValidationResult result = rule.validate(new String[]{"prop"}, mockContext());

        assertTrue(result.isValid());
    }

    @Test
    public void complexComposition_withConditional_shouldWorkCorrectly() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.hasProperty("environment")).thenReturn(true);
        when(context.getProperty("environment")).thenReturn(java.util.Optional.of("production"));

        // Should only fail if environment == production
        MultiPropertyValidationRule rule = MultiPropertyValidationRule
                .alwaysFails("Production error")
                .onlyIf(ctx -> ctx.getProperty("environment")
                        .orElse("").equals("production"));

        ValidationResult result = rule.validate(new String[]{"prop"}, context);

        assertFalse(result.isValid());
    }

    private PropertyContext mockContext() {
        return mock(PropertyContext.class);
    }
}
