package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ValidationRule}.
 */
public class ValidationRuleTest {

    private PropertyContext context;

    @Before
    public void setUp() {
        context = Mockito.mock(PropertyContext.class);
    }

    @Test
    public void alwaysValid_AlwaysReturnsSuccess() {
        ValidationRule<String> rule = ValidationRule.alwaysValid();

        ValidationResult result = rule.validate("test", "value", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void alwaysFails_AlwaysReturnsFailure() {
        ValidationRule<String> rule = ValidationRule.alwaysFails("Always fails");

        ValidationResult result = rule.validate("test", "value", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getErrorMessage()).isEqualTo("Always fails");
    }

    @Test
    public void and_BothPass_ReturnsSuccess() {
        ValidationRule<String> rule1 = ValidationRule.alwaysValid();
        ValidationRule<String> rule2 = ValidationRule.alwaysValid();

        ValidationRule<String> combined = rule1.and(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void and_FirstFails_ReturnsFirstFailure() {
        ValidationRule<String> rule1 = ValidationRule.alwaysFails("First fails");
        ValidationRule<String> rule2 = ValidationRule.alwaysValid();

        ValidationRule<String> combined = rule1.and(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).isEqualTo("First fails");
    }

    @Test
    public void and_SecondFails_ReturnsSecondFailure() {
        ValidationRule<String> rule1 = ValidationRule.alwaysValid();
        ValidationRule<String> rule2 = ValidationRule.alwaysFails("Second fails");

        ValidationRule<String> combined = rule1.and(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).isEqualTo("Second fails");
    }

    @Test
    public void or_FirstPasses_ReturnsSuccess() {
        ValidationRule<String> rule1 = ValidationRule.alwaysValid();
        ValidationRule<String> rule2 = ValidationRule.alwaysFails("Second fails");

        ValidationRule<String> combined = rule1.or(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void or_SecondPasses_ReturnsSuccess() {
        ValidationRule<String> rule1 = ValidationRule.alwaysFails("First fails");
        ValidationRule<String> rule2 = ValidationRule.alwaysValid();

        ValidationRule<String> combined = rule1.or(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void or_BothFail_ReturnsSecondFailure() {
        ValidationRule<String> rule1 = ValidationRule.alwaysFails("First fails");
        ValidationRule<String> rule2 = ValidationRule.alwaysFails("Second fails");

        ValidationRule<String> combined = rule1.or(rule2);
        ValidationResult result = combined.validate("test", "value", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).isEqualTo("Second fails");
    }

    @Test
    public void onlyIf_ConditionTrue_ExecutesRule() {
        ValidationRule<String> rule = ValidationRule.alwaysFails("Fails");
        ValidationRule<String> conditional = rule.onlyIf(ctx -> true);

        ValidationResult result = conditional.validate("test", "value", context);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void onlyIf_ConditionFalse_ReturnsSuccess() {
        ValidationRule<String> rule = ValidationRule.alwaysFails("Fails");
        ValidationRule<String> conditional = rule.onlyIf(ctx -> false);

        ValidationResult result = conditional.validate("test", "value", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void complexComposition_WorksCorrectly() {
        // (rule1 AND rule2) OR rule3
        ValidationRule<String> rule1 = (name, value, ctx) ->
                value != null && value.length() > 0
                        ? ValidationResult.success()
                        : ValidationResult.failure(ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value cannot be empty")
                                .build());

        ValidationRule<String> rule2 = (name, value, ctx) ->
                value != null && value.length() <= 10
                        ? ValidationResult.success()
                        : ValidationResult.failure(ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value too long")
                                .build());

        ValidationRule<String> rule3 = (name, value, ctx) ->
                "SKIP".equals(value)
                        ? ValidationResult.success()
                        : ValidationResult.failure(ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Not SKIP")
                                .build());

        ValidationRule<String> complex = rule1.and(rule2).or(rule3);

        // Valid short value
        assertThat(complex.validate("test", "hello", context).isValid()).isTrue();

        // Too long value but not SKIP
        assertThat(complex.validate("test", "verylongvalue", context).isValid()).isFalse();

        // SKIP value passes via rule3
        assertThat(complex.validate("test", "SKIP", context).isValid()).isTrue();

        // Empty value fails both paths
        assertThat(complex.validate("test", "", context).isValid()).isFalse();
    }
}
