package com.cleanconfig.core.validation;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ValidationResult}.
 */
public class ValidationResultTest {

    @Test
    public void success_ReturnsValidResult() {
        ValidationResult result = ValidationResult.success();

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getErrorCount()).isEqualTo(0);
    }

    @Test
    public void failure_WithSingleError_ReturnsInvalidResult() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).isEqualTo(error);
        assertThat(result.getErrorCount()).isEqualTo(1);
    }

    @Test
    public void failure_WithMultipleErrors_ReturnsInvalidResult() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("prop1")
                .errorMessage("Error 1")
                .build();
        ValidationError error2 = ValidationError.builder()
                .propertyName("prop2")
                .errorMessage("Error 2")
                .build();

        List<ValidationError> errors = Arrays.asList(error1, error2);
        ValidationResult result = ValidationResult.failure(errors);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).containsExactly(error1, error2);
        assertThat(result.getErrorCount()).isEqualTo(2);
    }

    @Test
    public void failure_WithNullError_ThrowsException() {
        assertThatThrownBy(() -> ValidationResult.failure((ValidationError) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void failure_WithNullErrors_ThrowsException() {
        assertThatThrownBy(() -> ValidationResult.failure((List<ValidationError>) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void failure_WithEmptyErrors_ThrowsException() {
        assertThatThrownBy(() -> ValidationResult.failure(Arrays.asList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    public void combine_TwoSuccesses_ReturnsSuccess() {
        ValidationResult result1 = ValidationResult.success();
        ValidationResult result2 = ValidationResult.success();

        ValidationResult combined = result1.combine(result2);

        assertThat(combined.isValid()).isTrue();
        assertThat(combined.getErrors()).isEmpty();
    }

    @Test
    public void combine_SuccessAndFailure_ReturnsFailure() {
        ValidationResult success = ValidationResult.success();
        ValidationResult failure = ValidationResult.failure(
                ValidationError.builder()
                        .propertyName("test")
                        .errorMessage("Error")
                        .build()
        );

        ValidationResult combined = success.combine(failure);

        assertThat(combined.isValid()).isFalse();
        assertThat(combined.getErrors()).hasSize(1);
    }

    @Test
    public void combine_TwoFailures_CombinesErrors() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("prop1")
                .errorMessage("Error 1")
                .build();
        ValidationError error2 = ValidationError.builder()
                .propertyName("prop2")
                .errorMessage("Error 2")
                .build();

        ValidationResult result1 = ValidationResult.failure(error1);
        ValidationResult result2 = ValidationResult.failure(error2);

        ValidationResult combined = result1.combine(result2);

        assertThat(combined.isValid()).isFalse();
        assertThat(combined.getErrors()).hasSize(2);
        assertThat(combined.getErrors()).containsExactly(error1, error2);
    }

    @Test
    public void combine_WithNull_ThrowsException() {
        ValidationResult result = ValidationResult.success();

        assertThatThrownBy(() -> result.combine(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void equals_SameResults_ReturnsTrue() {
        ValidationResult success1 = ValidationResult.success();
        ValidationResult success2 = ValidationResult.success();

        assertThat(success1).isEqualTo(success2);

        ValidationError error = ValidationError.builder()
                .propertyName("test")
                .errorMessage("Error")
                .build();
        ValidationResult failure1 = ValidationResult.failure(error);
        ValidationResult failure2 = ValidationResult.failure(error);

        assertThat(failure1).isEqualTo(failure2);
    }

    @Test
    public void toString_Success_ContainsValid() {
        ValidationResult result = ValidationResult.success();

        assertThat(result.toString()).contains("valid");
    }

    @Test
    public void toString_Failure_ContainsErrorCount() {
        ValidationResult result = ValidationResult.failure(
                ValidationError.builder()
                        .propertyName("test")
                        .errorMessage("Error")
                        .build()
        );

        assertThat(result.toString()).contains("errors=1");
    }
}
