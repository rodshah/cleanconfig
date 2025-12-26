package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NumericRules}.
 */
public class NumericRulesTest {

    private PropertyContext context;

    @Before
    public void setUp() {
        context = new TestPropertyContext();
    }

    // positive() tests
    @Test
    public void positive_PositiveValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.positive();
        assertThat(rule.validate("count", 1, context).isValid()).isTrue();
        assertThat(rule.validate("count", 100, context).isValid()).isTrue();
    }

    @Test
    public void positive_ZeroOrNegative_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.positive();
        ValidationResult result = rule.validate("count", 0, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be positive");

        assertThat(rule.validate("count", -5, context).isValid()).isFalse();
    }

    // negative() tests
    @Test
    public void negative_NegativeValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.negative();
        assertThat(rule.validate("delta", -1, context).isValid()).isTrue();
        assertThat(rule.validate("delta", -100, context).isValid()).isTrue();
    }

    @Test
    public void negative_ZeroOrPositive_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.negative();
        assertThat(rule.validate("delta", 0, context).isValid()).isFalse();
        assertThat(rule.validate("delta", 5, context).isValid()).isFalse();
    }

    // nonNegative() tests
    @Test
    public void nonNegative_ZeroOrPositive_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.nonNegative();
        assertThat(rule.validate("count", 0, context).isValid()).isTrue();
        assertThat(rule.validate("count", 10, context).isValid()).isTrue();
    }

    @Test
    public void nonNegative_Negative_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.nonNegative();
        ValidationResult result = rule.validate("count", -5, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("non-negative");
    }

    // nonPositive() tests
    @Test
    public void nonPositive_ZeroOrNegative_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.nonPositive();
        assertThat(rule.validate("offset", 0, context).isValid()).isTrue();
        assertThat(rule.validate("offset", -10, context).isValid()).isTrue();
    }

    @Test
    public void nonPositive_Positive_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.nonPositive();
        assertThat(rule.validate("offset", 5, context).isValid()).isFalse();
    }

    // zero() tests
    @Test
    public void zero_ZeroValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.zero();
        assertThat(rule.validate("value", 0, context).isValid()).isTrue();
    }

    @Test
    public void zero_NonZeroValue_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.zero();
        assertThat(rule.validate("value", 1, context).isValid()).isFalse();
        assertThat(rule.validate("value", -1, context).isValid()).isFalse();
    }

    // min() tests
    @Test
    public void min_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.min(10);
        assertThat(rule.validate("value", 10, context).isValid()).isTrue();
        assertThat(rule.validate("value", 20, context).isValid()).isTrue();
    }

    @Test
    public void min_TooSmall_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.min(10);
        ValidationResult result = rule.validate("value", 5, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("at least 10");
    }

    // max() tests
    @Test
    public void max_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.max(100);
        assertThat(rule.validate("value", 100, context).isValid()).isTrue();
        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
    }

    @Test
    public void max_TooLarge_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.max(100);
        ValidationResult result = rule.validate("value", 150, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not exceed 100");
    }

    // between() tests
    @Test
    public void between_ValidValue_ReturnsSuccess() {
        ValidationRule<Double> rule = NumericRules.between(0.0, 1.0);
        assertThat(rule.validate("percentage", 0.0, context).isValid()).isTrue();
        assertThat(rule.validate("percentage", 0.5, context).isValid()).isTrue();
        assertThat(rule.validate("percentage", 1.0, context).isValid()).isTrue();
    }

    @Test
    public void between_OutOfRange_ReturnsFailure() {
        ValidationRule<Double> rule = NumericRules.between(0.0, 1.0);
        assertThat(rule.validate("percentage", -0.1, context).isValid()).isFalse();
        assertThat(rule.validate("percentage", 1.1, context).isValid()).isFalse();
    }

    // integerBetween() tests
    @Test
    public void integerBetween_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.integerBetween(1, 100);
        assertThat(rule.validate("value", 1, context).isValid()).isTrue();
        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
        assertThat(rule.validate("value", 100, context).isValid()).isTrue();
    }

    @Test
    public void integerBetween_OutOfRange_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.integerBetween(1, 100);
        assertThat(rule.validate("value", 0, context).isValid()).isFalse();
        assertThat(rule.validate("value", 101, context).isValid()).isFalse();
    }

    // longBetween() tests
    @Test
    public void longBetween_ValidValue_ReturnsSuccess() {
        ValidationRule<Long> rule = NumericRules.longBetween(1000L, 9999L);
        assertThat(rule.validate("value", 1000L, context).isValid()).isTrue();
        assertThat(rule.validate("value", 5000L, context).isValid()).isTrue();
        assertThat(rule.validate("value", 9999L, context).isValid()).isTrue();
    }

    @Test
    public void longBetween_OutOfRange_ReturnsFailure() {
        ValidationRule<Long> rule = NumericRules.longBetween(1000L, 9999L);
        assertThat(rule.validate("value", 999L, context).isValid()).isFalse();
        assertThat(rule.validate("value", 10000L, context).isValid()).isFalse();
    }

    // greaterThan() tests
    @Test
    public void greaterThan_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.greaterThan(0);
        assertThat(rule.validate("value", 1, context).isValid()).isTrue();
        assertThat(rule.validate("value", 100, context).isValid()).isTrue();
    }

    @Test
    public void greaterThan_EqualOrLess_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.greaterThan(10);
        assertThat(rule.validate("value", 10, context).isValid()).isFalse(); // Equal
        assertThat(rule.validate("value", 5, context).isValid()).isFalse(); // Less
    }

    // lessThan() tests
    @Test
    public void lessThan_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.lessThan(100);
        assertThat(rule.validate("value", 99, context).isValid()).isTrue();
        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
    }

    @Test
    public void lessThan_EqualOrGreater_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.lessThan(100);
        assertThat(rule.validate("value", 100, context).isValid()).isFalse(); // Equal
        assertThat(rule.validate("value", 150, context).isValid()).isFalse(); // Greater
    }

    // port() tests
    @Test
    public void port_ValidPort_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.port();
        assertThat(rule.validate("port", 1, context).isValid()).isTrue();
        assertThat(rule.validate("port", 8080, context).isValid()).isTrue();
        assertThat(rule.validate("port", 65535, context).isValid()).isTrue();
    }

    @Test
    public void port_InvalidPort_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.port();
        assertThat(rule.validate("port", 0, context).isValid()).isFalse();
        assertThat(rule.validate("port", -1, context).isValid()).isFalse();
        assertThat(rule.validate("port", 65536, context).isValid()).isFalse();
    }

    // even() tests
    @Test
    public void even_EvenNumber_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.even();
        assertThat(rule.validate("value", 0, context).isValid()).isTrue();
        assertThat(rule.validate("value", 2, context).isValid()).isTrue();
        assertThat(rule.validate("value", -4, context).isValid()).isTrue();
    }

    @Test
    public void even_OddNumber_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.even();
        ValidationResult result = rule.validate("value", 3, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be even");
    }

    // odd() tests
    @Test
    public void odd_OddNumber_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.odd();
        assertThat(rule.validate("value", 1, context).isValid()).isTrue();
        assertThat(rule.validate("value", 3, context).isValid()).isTrue();
        assertThat(rule.validate("value", -5, context).isValid()).isTrue();
    }

    @Test
    public void odd_EvenNumber_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.odd();
        ValidationResult result = rule.validate("value", 4, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be odd");
    }

    // multipleOf() tests
    @Test
    public void multipleOf_ValidMultiple_ReturnsSuccess() {
        ValidationRule<Integer> rule = NumericRules.multipleOf(5);
        assertThat(rule.validate("value", 0, context).isValid()).isTrue();
        assertThat(rule.validate("value", 5, context).isValid()).isTrue();
        assertThat(rule.validate("value", 25, context).isValid()).isTrue();
    }

    @Test
    public void multipleOf_NotMultiple_ReturnsFailure() {
        ValidationRule<Integer> rule = NumericRules.multipleOf(5);
        ValidationResult result = rule.validate("value", 7, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("multiple of 5");
    }

    // Test composition
    @Test
    public void composition_MultipleRules_Works() {
        ValidationRule<Integer> rule = NumericRules.<Integer>positive()
                .and(NumericRules.even())
                .and(NumericRules.max(100));

        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
        assertThat(rule.validate("value", -2, context).isValid()).isFalse(); // Not positive
        assertThat(rule.validate("value", 3, context).isValid()).isFalse(); // Not even
        assertThat(rule.validate("value", 102, context).isValid()).isFalse(); // Too large
    }

    // Null value tests
    @Test
    public void allRules_NullValue_ReturnsSuccess() {
        // All numeric rules should pass for null values
        assertThat(NumericRules.<Integer>positive().validate("v", null, context).isValid()).isTrue();
        assertThat(NumericRules.<Integer>negative().validate("v", null, context).isValid()).isTrue();
        assertThat(NumericRules.<Integer>min(10).validate("v", null, context).isValid()).isTrue();
        assertThat(NumericRules.<Integer>max(10).validate("v", null, context).isValid()).isTrue();
    }

    // Simple test context implementation
    private static class TestPropertyContext implements PropertyContext {
        private final Map<String, String> properties = new HashMap<>();

        @Override
        public Optional<String> getProperty(String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }

        @Override
        public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
            return Optional.empty();
        }

        @Override
        public Map<String, String> getAllProperties() {
            return Collections.unmodifiableMap(properties);
        }

        @Override
        public ValidationContextType getContextType() {
            return ValidationContextType.STARTUP;
        }

        @Override
        public Optional<String> getMetadata(String key) {
            return Optional.empty();
        }

        @Override
        public boolean hasProperty(String propertyName) {
            return properties.containsKey(propertyName);
        }
    }
}
