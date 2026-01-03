package com.cleanconfig.core.validation.format;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JsonValidationFormatter}.
 */
public class JsonValidationFormatterTest {

    private final JsonValidationFormatter formatter = new JsonValidationFormatter();

    @Test
    public void format_ValidResult_ReturnsValidJson() {
        ValidationResult result = ValidationResult.success();

        String formatted = formatter.format(result);

        assertThat(formatted).contains("\"valid\": true");
        assertThat(formatted).contains("\"errorCount\": 0");
        assertThat(formatted).contains("\"errors\": []");
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

        assertThat(formatted).contains("\"valid\": false");
        assertThat(formatted).contains("\"errorCount\": 1");
        assertThat(formatted).contains("\"propertyName\": \"server.port\"");
        assertThat(formatted).contains("\"errorMessage\": \"Port must be between 1024 and 65535\"");
        assertThat(formatted).contains("\"actualValue\": \"80\"");
        assertThat(formatted).contains("\"expectedValue\": \"1024-65535\"");
        assertThat(formatted).contains("\"errorCode\": \"PORT_OUT_OF_RANGE\"");
        assertThat(formatted).contains("\"suggestion\": \"Use a port >= 1024 to avoid requiring root privileges\"");
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

        assertThat(formatted).contains("\"valid\": false");
        assertThat(formatted).contains("\"errorCount\": 2");
        assertThat(formatted).contains("\"propertyName\": \"server.port\"");
        assertThat(formatted).contains("\"propertyName\": \"db.url\"");
    }

    @Test
    public void format_ErrorWithOnlyRequiredFields_FormatsCorrectly() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        assertThat(formatted).contains("\"propertyName\": \"test.property\"");
        assertThat(formatted).contains("\"errorMessage\": \"Invalid value\"");
        // Optional fields should not appear when null
        assertThat(formatted).doesNotContain("\"actualValue\"");
        assertThat(formatted).doesNotContain("\"expectedValue\"");
        assertThat(formatted).doesNotContain("\"errorCode\"");
        assertThat(formatted).doesNotContain("\"suggestion\"");
    }

    @Test
    public void format_ErrorWithSpecialCharacters_EscapesCorrectly() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Value must not contain \"quotes\" or \\backslashes\\")
                .actualValue("Line 1\nLine 2\tTabbed")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        // Verify JSON escaping
        assertThat(formatted).contains("\\\"quotes\\\"");
        assertThat(formatted).contains("\\\\backslashes\\\\");
        assertThat(formatted).contains("\\n");
        assertThat(formatted).contains("\\t");
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

        assertThat(formatted).contains("\"suggestion\": \"Try using format: user@example.com\"");
    }

    @Test
    public void format_NullResult_ThrowsException() {
        assertThatThrownBy(() -> formatter.format(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("result cannot be null");
    }

    @Test
    public void format_ProducesValidJson_CanBeParsed() {
        ValidationError error = ValidationError.builder()
                .propertyName("server.port")
                .errorMessage("Invalid port")
                .actualValue("80")
                .build();

        ValidationResult result = ValidationResult.failure(error);

        String formatted = formatter.format(result);

        // Basic validation that the output is valid JSON structure
        assertThat(formatted).startsWith("{");
        assertThat(formatted).endsWith("}");
        assertThat(formatted.chars().filter(ch -> ch == '{').count())
                .isEqualTo(formatted.chars().filter(ch -> ch == '}').count());
        assertThat(formatted.chars().filter(ch -> ch == '[').count())
                .isEqualTo(formatted.chars().filter(ch -> ch == ']').count());
    }
}
