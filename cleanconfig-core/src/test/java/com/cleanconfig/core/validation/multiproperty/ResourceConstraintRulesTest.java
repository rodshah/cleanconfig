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
 * Tests for ResourceConstraintRules.
 */
public class ResourceConstraintRulesTest {

    @Test
    public void cpuRequestLimit_shouldPassWhenRequestLessThanLimit() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("cpu.request", Integer.class)).thenReturn(Optional.of(2));
        when(context.getTypedProperty("cpu.limit", Integer.class)).thenReturn(Optional.of(4));

        MultiPropertyValidationRule rule = ResourceConstraintRules.cpuRequestLimit(
                "cpu.request", "cpu.limit");

        ValidationResult result = rule.validate(
                new String[]{"cpu.request", "cpu.limit"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void cpuRequestLimit_shouldPassWhenRequestEqualsLimit() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("cpu.request", Integer.class)).thenReturn(Optional.of(4));
        when(context.getTypedProperty("cpu.limit", Integer.class)).thenReturn(Optional.of(4));

        MultiPropertyValidationRule rule = ResourceConstraintRules.cpuRequestLimit(
                "cpu.request", "cpu.limit");

        ValidationResult result = rule.validate(
                new String[]{"cpu.request", "cpu.limit"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void cpuRequestLimit_shouldFailWhenRequestGreaterThanLimit() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("cpu.request", Integer.class)).thenReturn(Optional.of(8));
        when(context.getTypedProperty("cpu.limit", Integer.class)).thenReturn(Optional.of(4));

        MultiPropertyValidationRule rule = ResourceConstraintRules.cpuRequestLimit(
                "cpu.request", "cpu.limit");

        ValidationResult result = rule.validate(
                new String[]{"cpu.request", "cpu.limit"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void memoryRequestLimit_shouldPassWhenRequestLessThanLimit() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("memory.request", Long.class))
                .thenReturn(Optional.of(1024L * 1024L * 512L)); // 512 MB
        when(context.getTypedProperty("memory.limit", Long.class))
                .thenReturn(Optional.of(1024L * 1024L * 1024L)); // 1 GB

        MultiPropertyValidationRule rule = ResourceConstraintRules.memoryRequestLimit(
                "memory.request", "memory.limit");

        ValidationResult result = rule.validate(
                new String[]{"memory.request", "memory.limit"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void memoryRequestLimit_shouldFailWhenRequestGreaterThanLimit() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("memory.request", Long.class))
                .thenReturn(Optional.of(1024L * 1024L * 2048L)); // 2 GB
        when(context.getTypedProperty("memory.limit", Long.class))
                .thenReturn(Optional.of(1024L * 1024L * 1024L)); // 1 GB

        MultiPropertyValidationRule rule = ResourceConstraintRules.memoryRequestLimit(
                "memory.request", "memory.limit");

        ValidationResult result = rule.validate(
                new String[]{"memory.request", "memory.limit"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void validRange_shouldPassWhenMinLessThanMax() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("port.min", Integer.class)).thenReturn(Optional.of(8000));
        when(context.getTypedProperty("port.max", Integer.class)).thenReturn(Optional.of(9000));

        MultiPropertyValidationRule rule = ResourceConstraintRules.validRange(
                "port.min", "port.max", Integer.class);

        ValidationResult result = rule.validate(
                new String[]{"port.min", "port.max"}, context);

        assertTrue(result.isValid());
    }

    @Test
    public void validRange_shouldFailWhenMinEqualsMax() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("port.min", Integer.class)).thenReturn(Optional.of(8000));
        when(context.getTypedProperty("port.max", Integer.class)).thenReturn(Optional.of(8000));

        MultiPropertyValidationRule rule = ResourceConstraintRules.validRange(
                "port.min", "port.max", Integer.class);

        ValidationResult result = rule.validate(
                new String[]{"port.min", "port.max"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void validRange_shouldFailWhenMinGreaterThanMax() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("port.min", Integer.class)).thenReturn(Optional.of(9000));
        when(context.getTypedProperty("port.max", Integer.class)).thenReturn(Optional.of(8000));

        MultiPropertyValidationRule rule = ResourceConstraintRules.validRange(
                "port.min", "port.max", Integer.class);

        ValidationResult result = rule.validate(
                new String[]{"port.min", "port.max"}, context);

        assertFalse(result.isValid());
    }

    @Test
    public void validRange_withDouble_shouldWorkCorrectly() {
        PropertyContext context = mock(PropertyContext.class);
        when(context.getTypedProperty("price.min", Double.class)).thenReturn(Optional.of(9.99));
        when(context.getTypedProperty("price.max", Double.class)).thenReturn(Optional.of(99.99));

        MultiPropertyValidationRule rule = ResourceConstraintRules.validRange(
                "price.min", "price.max", Double.class);

        ValidationResult result = rule.validate(
                new String[]{"price.min", "price.max"}, context);

        assertTrue(result.isValid());
    }
}
