package com.cleanconfig.core.validation.format;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TextValidationFormatter}.
 */
public class TextValidationFormatterTest {

    private final TextValidationFormatter formatter = new TextValidationFormatter();

    @Test
    public void format_ValidResult_ReturnsSuccessMessage() {
        ValidationResult result = ValidationResult.success();

        String formatted = formatter.format(result);

        assertThat(formatted).isEqualTo("Validation passed: 0 errors");
    }

    @Test
    public void format_SingleError_FormatsCorrectly() {
        ValidationError error = ValidationError.builder()
                .propertyName("server.port")
                .errorMessage("Port must be between 1024 and 65535")
                .actualValue("80")
                .expectedValue("1024-65535")
                .errorCode("PORT_OUT_OF_RANGE")
                .suggestion("Use a port >= 1024 to avoid requiring root privileges")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        assertThat(formatted).contains("Validation failed with 1 error:");
        assertThat(formatted).contains("Error 1: server.port");
        assertThat(formatted).contains("Message: Port must be between 1024 and 65535");
        assertThat(formatted).contains("Actual: 80");
        assertThat(formatted).contains("Expected: 1024-65535");
        assertThat(formatted).contains("Code: PORT_OUT_OF_RANGE");
        assertThat(formatted).contains("Suggestion: Use a port >= 1024 to avoid requiring root privileges");
    }

    @Test
    public void format_MultipleErrors_FormatsCorrectly() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("server.port")
                .errorMessage("Port out of range")
                .actualValue("80")
                .build();

        ValidationError error2 = ValidationError.builder()
                .propertyName("db.url")
                .errorMessage("Invalid URL")
                .actualValue("invalid-url")
                .build();

        ValidationResult result = ValidationResult.failure(error1).combine(ValidationResult.failure(error2));

        String formatted = formatter.format(result);

        assertThat(formatted).contains("Validation failed with 2 errors:");
        assertThat(formatted).contains("Error 1: server.port");
        assertThat(formatted).contains("Error 2: db.url");
        assertThat(formatted).contains("Port out of range");
        assertThat(formatted).contains("Invalid URL");
    }

    @Test
    public void format_ErrorWithOnlyRequiredFields_FormatsCorrectly() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        assertThat(formatted).contains("Error 1: test.property");
        assertThat(formatted).contains("Message: Invalid value");
        assertThat(formatted).doesNotContain("Actual:");
        assertThat(formatted).doesNotContain("Expected:");
        assertThat(formatted).doesNotContain("Code:");
        assertThat(formatted).doesNotContain("Suggestion:");
    }

    @Test
    public void format_ErrorWithSuggestion_IncludesSuggestion() {
        ValidationError error = ValidationError.builder()
                .propertyName("email")
                .errorMessage("Invalid email format")
                .suggestion("Try using format: user@example.com")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        assertThat(formatted).contains("Suggestion: Try using format: user@example.com");
    }

    @Test
    public void format_NullResult_ThrowsException() {
        assertThatThrownBy(() -> formatter.format(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("result cannot be null");
    }
}
