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
 * Tests for NumericRelationshipRules.
 */
public class NumericRelationshipRulesTest {

    @Test
    public void lessThan_shouldPassWhenFirstLessThanSecond() {
        PropertyContext context = mockContext(5, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void lessThan_shouldFailWhenFirstEqualsSecond() {
        PropertyContext context = mockContext(10, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("must be less than"));
    }

    @Test
    public void lessThan_shouldFailWhenFirstGreaterThanSecond() {
        PropertyContext context = mockContext(15, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void lessThan_shouldPassWhenEitherPropertyMissing() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("min", Integer.class)).thenReturn(Optional.empty());
        when(context.getTypedProperty("max", Integer.class)).thenReturn(Optional.of(10));

        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void lessThanOrEqual_shouldPassWhenFirstLessThanSecond() {
        PropertyContext context = mockContext(5, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThanOrEqual(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void lessThanOrEqual_shouldPassWhenFirstEqualsSecond() {
        PropertyContext context = mockContext(10, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThanOrEqual(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void lessThanOrEqual_shouldFailWhenFirstGreaterThanSecond() {
        PropertyContext context = mockContext(15, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThanOrEqual(
                "min", "max", Integer.class);

        ValidationResult result = rule.validate(new String[]{"min", "max"}, context);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getErrorMessage()
                .contains("must be less than or equal to"));
    }

    @Test
    public void greaterThan_shouldPassWhenFirstGreaterThanSecond() {
        PropertyContext context = mockContext(15, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.greaterThan(
                "first", "second", Integer.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void greaterThan_shouldFailWhenFirstEqualsSecond() {
        PropertyContext context = mockContext(10, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.greaterThan(
                "first", "second", Integer.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void greaterThanOrEqual_shouldPassWhenFirstGreaterThanSecond() {
        PropertyContext context = mockContext(15, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.greaterThanOrEqual(
                "first", "second", Integer.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void greaterThanOrEqual_shouldPassWhenFirstEqualsSecond() {
        PropertyContext context = mockContext(10, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.greaterThanOrEqual(
                "first", "second", Integer.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void greaterThanOrEqual_shouldFailWhenFirstLessThanSecond() {
        PropertyContext context = mockContext(5, 10);
        MultiPropertyValidationRule rule = NumericRelationshipRules.greaterThanOrEqual(
                "first", "second", Integer.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void lessThan_withLong_shouldWorkCorrectly() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("first", Long.class)).thenReturn(Optional.of(100L));
        when(context.getTypedProperty("second", Long.class)).thenReturn(Optional.of(200L));

        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "first", "second", Long.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void lessThan_withDouble_shouldWorkCorrectly() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("first", Double.class)).thenReturn(Optional.of(2.5));
        when(context.getTypedProperty("second", Double.class)).thenReturn(Optional.of(5.0));

        MultiPropertyValidationRule rule = NumericRelationshipRules.lessThan(
                "first", "second", Double.class);

        ValidationResult result = rule.validate(new String[]{"first", "second"}, context);

        assertTrue(result.isValid());
    }

    private PropertyContext mockContext(int first, int second) {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("min", Integer.class)).thenReturn(Optional.of(first));
        when(context.getTypedProperty("max", Integer.class)).thenReturn(Optional.of(second));
        when(context.getTypedProperty("first", Integer.class)).thenReturn(Optional.of(first));
        when(context.getTypedProperty("second", Integer.class)).thenReturn(Optional.of(second));
        return context;
    }
}
