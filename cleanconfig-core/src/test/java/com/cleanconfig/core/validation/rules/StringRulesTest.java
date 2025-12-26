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
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StringRules}.
 */
public class StringRulesTest {

    private PropertyContext context;

    @Before
    public void setUp() {
        context = new TestPropertyContext();
    }

    // notBlank() tests
    @Test
    public void notBlank_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.notBlank();
        assertThat(rule.validate("name", "value", context).isValid()).isTrue();
        assertThat(rule.validate("name", "  value  ", context).isValid()).isTrue();
    }

    @Test
    public void notBlank_NullValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.notBlank();
        ValidationResult result = rule.validate("name", null, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("cannot be blank");
    }

    @Test
    public void notBlank_EmptyValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.notBlank();
        assertThat(rule.validate("name", "", context).isValid()).isFalse();
        assertThat(rule.validate("name", "   ", context).isValid()).isFalse();
    }

    // notEmpty() tests
    @Test
    public void notEmpty_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.notEmpty();
        assertThat(rule.validate("name", "value", context).isValid()).isTrue();
        assertThat(rule.validate("name", " ", context).isValid()).isTrue(); // Whitespace OK for notEmpty
    }

    @Test
    public void notEmpty_EmptyValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.notEmpty();
        ValidationResult result = rule.validate("name", "", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("cannot be empty");
    }

    // minLength() tests
    @Test
    public void minLength_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.minLength(5);
        assertThat(rule.validate("name", "hello", context).isValid()).isTrue();
        assertThat(rule.validate("name", "hello world", context).isValid()).isTrue();
    }

    @Test
    public void minLength_TooShort_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.minLength(5);
        ValidationResult result = rule.validate("name", "hi", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("at least 5");
    }

    @Test
    public void minLength_NullValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.minLength(5);
        assertThat(rule.validate("name", null, context).isValid()).isTrue();
    }

    // maxLength() tests
    @Test
    public void maxLength_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.maxLength(10);
        assertThat(rule.validate("name", "hello", context).isValid()).isTrue();
        assertThat(rule.validate("name", "1234567890", context).isValid()).isTrue();
    }

    @Test
    public void maxLength_TooLong_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.maxLength(5);
        ValidationResult result = rule.validate("name", "too long value", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not exceed 5");
    }

    // lengthBetween() tests
    @Test
    public void lengthBetween_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.lengthBetween(5, 10);
        assertThat(rule.validate("name", "hello", context).isValid()).isTrue();
        assertThat(rule.validate("name", "1234567890", context).isValid()).isTrue();
    }

    @Test
    public void lengthBetween_OutOfRange_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.lengthBetween(5, 10);
        assertThat(rule.validate("name", "hi", context).isValid()).isFalse();
        assertThat(rule.validate("name", "12345678901", context).isValid()).isFalse();
    }

    // matchesRegex() tests
    @Test
    public void matchesRegex_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.matchesRegex("^[a-z]+$");
        assertThat(rule.validate("name", "abc", context).isValid()).isTrue();
    }

    @Test
    public void matchesRegex_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.matchesRegex("^[a-z]+$");
        ValidationResult result = rule.validate("name", "ABC123", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("does not match pattern");
    }

    // matchesPattern() tests
    @Test
    public void matchesPattern_ValidValue_ReturnsSuccess() {
        Pattern pattern = Pattern.compile("^[0-9]+$");
        ValidationRule<String> rule = StringRules.matchesPattern(pattern);
        assertThat(rule.validate("name", "12345", context).isValid()).isTrue();
    }

    @Test
    public void matchesPattern_InvalidValue_ReturnsFailure() {
        Pattern pattern = Pattern.compile("^[0-9]+$");
        ValidationRule<String> rule = StringRules.matchesPattern(pattern);
        assertThat(rule.validate("name", "abc123", context).isValid()).isFalse();
    }

    // email() tests
    @Test
    public void email_ValidEmail_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.email();
        assertThat(rule.validate("email", "user@example.com", context).isValid()).isTrue();
        assertThat(rule.validate("email", "test.user+tag@example.co.uk", context).isValid()).isTrue();
    }

    @Test
    public void email_InvalidEmail_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.email();
        assertThat(rule.validate("email", "invalid", context).isValid()).isFalse();
        assertThat(rule.validate("email", "@example.com", context).isValid()).isFalse();
        assertThat(rule.validate("email", "user@", context).isValid()).isFalse();
    }

    // url() tests
    @Test
    public void url_ValidURL_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.url();
        assertThat(rule.validate("url", "https://example.com", context).isValid()).isTrue();
        assertThat(rule.validate("url", "http://localhost:8080/path", context).isValid()).isTrue();
    }

    @Test
    public void url_InvalidURL_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.url();
        ValidationResult result = rule.validate("url", "not a url", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not a valid URL");
    }

    // startsWith() tests
    @Test
    public void startsWith_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.startsWith("http");
        assertThat(rule.validate("url", "https://example.com", context).isValid()).isTrue();
    }

    @Test
    public void startsWith_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.startsWith("http");
        ValidationResult result = rule.validate("url", "ftp://example.com", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must start with: http");
    }

    // endsWith() tests
    @Test
    public void endsWith_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.endsWith(".txt");
        assertThat(rule.validate("file", "readme.txt", context).isValid()).isTrue();
    }

    @Test
    public void endsWith_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.endsWith(".txt");
        ValidationResult result = rule.validate("file", "readme.md", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must end with: .txt");
    }

    // contains() tests
    @Test
    public void contains_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.contains("example");
        assertThat(rule.validate("domain", "user@example.com", context).isValid()).isTrue();
    }

    @Test
    public void contains_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.contains("example");
        ValidationResult result = rule.validate("domain", "user@test.com", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must contain: example");
    }

    // doesNotContain() tests
    @Test
    public void doesNotContain_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.doesNotContain("admin");
        assertThat(rule.validate("username", "user123", context).isValid()).isTrue();
    }

    @Test
    public void doesNotContain_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.doesNotContain("admin");
        ValidationResult result = rule.validate("username", "admin_user", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must not contain: admin");
    }

    // alphanumeric() tests
    @Test
    public void alphanumeric_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.alphanumeric();
        assertThat(rule.validate("code", "ABC123", context).isValid()).isTrue();
    }

    @Test
    public void alphanumeric_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.alphanumeric();
        assertThat(rule.validate("code", "ABC-123", context).isValid()).isFalse();
        assertThat(rule.validate("code", "hello!", context).isValid()).isFalse();
    }

    // alphabetic() tests
    @Test
    public void alphabetic_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.alphabetic();
        assertThat(rule.validate("name", "HelloWorld", context).isValid()).isTrue();
    }

    @Test
    public void alphabetic_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.alphabetic();
        assertThat(rule.validate("name", "Hello123", context).isValid()).isFalse();
    }

    // numeric() tests
    @Test
    public void numeric_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.numeric();
        assertThat(rule.validate("code", "123456", context).isValid()).isTrue();
    }

    @Test
    public void numeric_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.numeric();
        assertThat(rule.validate("code", "12.34", context).isValid()).isFalse();
        assertThat(rule.validate("code", "12A34", context).isValid()).isFalse();
    }

    // lowercase() tests
    @Test
    public void lowercase_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.lowercase();
        assertThat(rule.validate("name", "lowercase", context).isValid()).isTrue();
    }

    @Test
    public void lowercase_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.lowercase();
        ValidationResult result = rule.validate("name", "LowerCase", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be lowercase");
    }

    // uppercase() tests
    @Test
    public void uppercase_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = StringRules.uppercase();
        assertThat(rule.validate("name", "UPPERCASE", context).isValid()).isTrue();
    }

    @Test
    public void uppercase_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = StringRules.uppercase();
        ValidationResult result = rule.validate("name", "UpperCase", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be uppercase");
    }

    // Test composition
    @Test
    public void composition_MultipleRules_Works() {
        ValidationRule<String> rule = StringRules.notBlank()
                .and(StringRules.minLength(5))
                .and(StringRules.endsWith("@company.com"));

        assertThat(rule.validate("email", "user@company.com", context).isValid()).isTrue();
        assertThat(rule.validate("email", "", context).isValid()).isFalse(); // Blank
        assertThat(rule.validate("email", "a@c", context).isValid()).isFalse(); // Too short (3 chars)
        assertThat(rule.validate("email", "user@example.com", context).isValid()).isFalse(); // Wrong ending
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
