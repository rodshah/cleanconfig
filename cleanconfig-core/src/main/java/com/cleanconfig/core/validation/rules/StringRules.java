package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Validation rules for string values.
 *
 * <p>Provides rules for string validation including length checks, pattern matching,
 * and format validation (email, URL, etc.).
 *
 * <p>Example usage:
 * <pre>
 * ValidationRule&lt;String&gt; emailRule = StringRules.notBlank()
 *     .and(StringRules.email())
 *     .and(StringRules.endsWith("@company.com"));
 * </pre>
 *
 * @since 0.1.0
 */
public final class StringRules {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
    );

    private StringRules() {
        // Utility class
    }

    /**
     * Validates that a string is not blank (not null and not empty after trimming).
     *
     * 
     * @return validation rule
     */
    public static ValidationRule<String> notBlank() {
        return (name, value, context) -> {
            if (value == null || value.toString().trim().isEmpty()) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value cannot be blank")
                                .actualValue(value == null ? "null" : "\"" + value + "\"")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string is not empty (not null and length > 0, no trimming).
     *
     * 
     * @return validation rule
     */
    public static ValidationRule<String> notEmpty() {
        return (name, value, context) -> {
            if (value == null || value.length() == 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value cannot be empty")
                                .actualValue(value == null ? "null" : "\"\"")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates minimum string length.
     *
     * @param minLength minimum length (inclusive)
     * 
     * @return validation rule
     */
    public static ValidationRule<String> minLength(int minLength) {
        return (name, value, context) -> {
            if (value != null && value.length() < minLength) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value length must be at least " + minLength)
                                .actualValue(String.valueOf(value.length()))
                                .expectedValue(">= " + minLength)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates maximum string length.
     *
     * @param maxLength maximum length (inclusive)
     * 
     * @return validation rule
     */
    public static ValidationRule<String> maxLength(int maxLength) {
        return (name, value, context) -> {
            if (value != null && value.length() > maxLength) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value length must not exceed " + maxLength)
                                .actualValue(String.valueOf(value.length()))
                                .expectedValue("<= " + maxLength)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string length is within a range (inclusive).
     *
     * @param minLength minimum length
     * @param maxLength maximum length
     * 
     * @return validation rule
     */
    public static ValidationRule<String> lengthBetween(int minLength, int maxLength) {
        return (name, value, context) -> {
            if (value != null) {
                int len = value.length();
                if (len < minLength || len > maxLength) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Value length must be between " + minLength + " and " + maxLength)
                                    .actualValue(String.valueOf(len))
                                    .expectedValue("[" + minLength + ", " + maxLength + "]")
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string matches a regular expression.
     *
     * @param regex the regular expression pattern
     * @return validation rule
     */
    public static ValidationRule<String> matchesRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return (name, value, context) -> {
            if (value != null && !pattern.matcher(value).matches()) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value does not match pattern: " + regex)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string matches a compiled pattern.
     *
     * @param pattern the compiled regex pattern
     * @return validation rule
     */
    public static ValidationRule<String> matchesPattern(Pattern pattern) {
        return (name, value, context) -> {
            if (value != null && !pattern.matcher(value).matches()) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value does not match pattern: " + pattern.pattern())
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string is a valid email address.
     *
     * @return validation rule
     */
    public static ValidationRule<String> email() {
        return (name, value, context) -> {
            if (value != null && !EMAIL_PATTERN.matcher(value).matches()) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value is not a valid email address")
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string is a valid URL.
     *
     * @return validation rule
     */
    public static ValidationRule<String> url() {
        return (name, value, context) -> {
            if (value != null) {
                try {
                    new URL(value);
                } catch (MalformedURLException e) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Value is not a valid URL")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string starts with a specific prefix.
     *
     * @param prefix the required prefix
     * @return validation rule
     */
    public static ValidationRule<String> startsWith(String prefix) {
        return (name, value, context) -> {
            if (value != null && !value.startsWith(prefix)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must start with: " + prefix)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string ends with a specific suffix.
     *
     * @param suffix the required suffix
     * @return validation rule
     */
    public static ValidationRule<String> endsWith(String suffix) {
        return (name, value, context) -> {
            if (value != null && !value.endsWith(suffix)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must end with: " + suffix)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string contains a specific substring.
     *
     * @param substring the required substring
     * @return validation rule
     */
    public static ValidationRule<String> contains(String substring) {
        return (name, value, context) -> {
            if (value != null && !value.contains(substring)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must contain: " + substring)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string does not contain a specific substring.
     *
     * @param substring the forbidden substring
     * @return validation rule
     */
    public static ValidationRule<String> doesNotContain(String substring) {
        return (name, value, context) -> {
            if (value != null && value.contains(substring)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must not contain: " + substring)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string is alphanumeric (only letters and digits).
     *
     * @return validation rule
     */
    public static ValidationRule<String> alphanumeric() {
        return matchesRegex("^[a-zA-Z0-9]+$");
    }

    /**
     * Validates that a string is alphabetic (only letters).
     *
     * @return validation rule
     */
    public static ValidationRule<String> alphabetic() {
        return matchesRegex("^[a-zA-Z]+$");
    }

    /**
     * Validates that a string is numeric (only digits).
     *
     * @return validation rule
     */
    public static ValidationRule<String> numeric() {
        return matchesRegex("^[0-9]+$");
    }

    /**
     * Validates that a string is lowercase.
     *
     * @return validation rule
     */
    public static ValidationRule<String> lowercase() {
        return (name, value, context) -> {
            if (value != null && !value.equals(value.toLowerCase())) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be lowercase")
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a string is uppercase.
     *
     * @return validation rule
     */
    public static ValidationRule<String> uppercase() {
        return (name, value, context) -> {
            if (value != null && !value.equals(value.toUpperCase())) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be uppercase")
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }
}
