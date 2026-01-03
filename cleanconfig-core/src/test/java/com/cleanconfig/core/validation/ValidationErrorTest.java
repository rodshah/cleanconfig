package com.cleanconfig.core.validation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ValidationError}.
 */
public class ValidationErrorTest {

    @Test
    public void builder_WithRequiredFields_CreatesError() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        assertThat(error.getPropertyName()).isEqualTo("test.property");
        assertThat(error.getErrorMessage()).isEqualTo("Invalid value");
        assertThat(error.getActualValue()).isNull();
        assertThat(error.getExpectedValue()).isNull();
        assertThat(error.getErrorCode()).isNull();
    }

    @Test
    public void builder_WithAllFields_CreatesError() {
        ValidationError error = ValidationError.builder()
                .propertyName("server.port")
                .errorMessage("Port out of range")
                .actualValue("80")
                .expectedValue("1024-65535")
                .errorCode("PORT_OUT_OF_RANGE")
                .suggestion("Use a port >= 1024 to avoid requiring root privileges")
                .build();

        assertThat(error.getPropertyName()).isEqualTo("server.port");
        assertThat(error.getErrorMessage()).isEqualTo("Port out of range");
        assertThat(error.getActualValue()).isEqualTo("80");
        assertThat(error.getExpectedValue()).isEqualTo("1024-65535");
        assertThat(error.getErrorCode()).isEqualTo("PORT_OUT_OF_RANGE");
        assertThat(error.getSuggestion()).isEqualTo("Use a port >= 1024 to avoid requiring root privileges");
    }

    @Test
    public void builder_WithSuggestion_SetsSuggestion() {
        ValidationError error = ValidationError.builder()
                .propertyName("email")
                .errorMessage("Invalid email format")
                .suggestion("Try using format: user@example.com")
                .build();

        assertThat(error.getSuggestion()).isEqualTo("Try using format: user@example.com");
    }

    @Test
    public void builder_WithoutSuggestion_SuggestionIsNull() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        assertThat(error.getSuggestion()).isNull();
    }

    @Test
    public void builder_WithoutPropertyName_ThrowsException() {
        assertThatThrownBy(() ->
                ValidationError.builder()
                        .errorMessage("Invalid value")
                        .build()
        ).isInstanceOf(NullPointerException.class)
         .hasMessageContaining("propertyName");
    }

    @Test
    public void builder_WithoutErrorMessage_ThrowsException() {
        assertThatThrownBy(() ->
                ValidationError.builder()
                        .propertyName("test.property")
                        .build()
        ).isInstanceOf(NullPointerException.class)
         .hasMessageContaining("errorMessage");
    }

    @Test
    public void equals_SameValues_ReturnsTrue() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("test")
                .errorMessage("Error")
                .actualValue("value")
                .suggestion("Fix this")
                .build();

        ValidationError error2 = ValidationError.builder()
                .propertyName("test")
                .errorMessage("Error")
                .actualValue("value")
                .suggestion("Fix this")
                .build();

        assertThat(error1).isEqualTo(error2);
        assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
    }

    @Test
    public void equals_DifferentSuggestions_ReturnsFalse() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("test")
                .errorMessage("Error")
                .suggestion("Suggestion 1")
                .build();

        ValidationError error2 = ValidationError.builder()
                .propertyName("test")
                .errorMessage("Error")
                .suggestion("Suggestion 2")
                .build();

        assertThat(error1).isNotEqualTo(error2);
    }

    @Test
    public void equals_DifferentValues_ReturnsFalse() {
        ValidationError error1 = ValidationError.builder()
                .propertyName("test1")
                .errorMessage("Error")
                .build();

        ValidationError error2 = ValidationError.builder()
                .propertyName("test2")
                .errorMessage("Error")
                .build();

        assertThat(error1).isNotEqualTo(error2);
    }

    @Test
    public void toString_WithMinimalFields_ContainsBasicInfo() {
        ValidationError error = ValidationError.builder()
                .propertyName("test.property")
                .errorMessage("Invalid value")
                .build();

        String str = error.toString();
        assertThat(str).contains("test.property");
        assertThat(str).contains("Invalid value");
    }

    @Test
    public void toString_WithAllFields_ContainsAllInfo() {
        ValidationError error = ValidationError.builder()
                .propertyName("server.port")
                .errorMessage("Port out of range")
                .actualValue("80")
                .expectedValue("1024-65535")
                .errorCode("PORT_OUT_OF_RANGE")
                .suggestion("Use port >= 1024")
                .build();

        String str = error.toString();
        assertThat(str).contains("server.port");
        assertThat(str).contains("Port out of range");
        assertThat(str).contains("80");
        assertThat(str).contains("1024-65535");
        assertThat(str).contains("PORT_OUT_OF_RANGE");
        assertThat(str).contains("Use port >= 1024");
    }

    @Test
    public void toString_WithSuggestion_ContainsSuggestion() {
        ValidationError error = ValidationError.builder()
                .propertyName("email")
                .errorMessage("Invalid email")
                .suggestion("Try format: user@example.com")
                .build();

        String str = error.toString();
        assertThat(str).contains("suggestion='Try format: user@example.com'");
    }
}
