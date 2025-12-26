package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.validation.rules.FileRules;
import com.cleanconfig.core.validation.rules.GeneralRules;
import com.cleanconfig.core.validation.rules.NumericRules;
import com.cleanconfig.core.validation.rules.StringRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Facade providing convenient access to all validation rules.
 *
 * <p>This class delegates to specific rule categories ({@link StringRules},
 * {@link NumericRules}, {@link FileRules}, {@link GeneralRules}) for better
 * organization and maintainability.
 *
 * <p>Users can choose between:
 * <ul>
 *   <li><strong>Facade (recommended for beginners):</strong> {@code Rules.notBlank()}</li>
 *   <li><strong>Direct (recommended for power users):</strong> {@code StringRules.notBlank()}</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Using facade
 * ValidationRule&lt;String&gt; emailRule = Rules.notBlank()
 *     .and(Rules.email())
 *     .and(Rules.endsWith("@company.com"));
 *
 * ValidationRule&lt;Integer&gt; portRule = Rules.port();
 *
 * // Using category classes directly
 * ValidationRule&lt;String&gt; pathRule = FileRules.fileExists()
 *     .and(FileRules.readable());
 * </pre>
 *
 * @since 0.1.0
 * @see StringRules
 * @see NumericRules
 * @see FileRules
 * @see GeneralRules
 */
public final class Rules {

    private Rules() {
        // Utility class
    }

    // ==================== String Rules ====================

    /**
     * Validates that a string is not blank (not null and not empty after trimming).
     *
     * 
     * @return validation rule
     * @see StringRules#notBlank()
     */
    public static ValidationRule<String> notBlank() {
        return StringRules.notBlank();
    }

    /**
     * Validates that a string is not empty (not null and length > 0, no trimming).
     *
     * 
     * @return validation rule
     * @see StringRules#notEmpty()
     */
    public static ValidationRule<String> notEmpty() {
        return StringRules.notEmpty();
    }

    /**
     * Validates minimum string length.
     *
     * @param minLength minimum length (inclusive)
     * 
     * @return validation rule
     * @see StringRules#minLength(int)
     */
    public static ValidationRule<String> minLength(int minLength) {
        return StringRules.minLength(minLength);
    }

    /**
     * Validates maximum string length.
     *
     * @param maxLength maximum length (inclusive)
     * 
     * @return validation rule
     * @see StringRules#maxLength(int)
     */
    public static ValidationRule<String> maxLength(int maxLength) {
        return StringRules.maxLength(maxLength);
    }

    /**
     * Validates that a string length is within a range (inclusive).
     *
     * @param minLength minimum length
     * @param maxLength maximum length
     * 
     * @return validation rule
     * @see StringRules#lengthBetween(int, int)
     */
    public static ValidationRule<String> lengthBetween(int minLength, int maxLength) {
        return StringRules.lengthBetween(minLength, maxLength);
    }

    /**
     * Validates that a string matches a regular expression.
     *
     * @param regex the regular expression pattern
     * @return validation rule
     * @see StringRules#matchesRegex(String)
     */
    public static ValidationRule<String> matchesRegex(String regex) {
        return StringRules.matchesRegex(regex);
    }

    /**
     * Validates that a string matches a compiled pattern.
     *
     * @param pattern the compiled regex pattern
     * @return validation rule
     * @see StringRules#matchesPattern(Pattern)
     */
    public static ValidationRule<String> matchesPattern(Pattern pattern) {
        return StringRules.matchesPattern(pattern);
    }

    /**
     * Validates that a string is a valid email address.
     *
     * @return validation rule
     * @see StringRules#email()
     */
    public static ValidationRule<String> email() {
        return StringRules.email();
    }

    /**
     * Validates that a string is a valid URL.
     *
     * @return validation rule
     * @see StringRules#url()
     */
    public static ValidationRule<String> url() {
        return StringRules.url();
    }

    /**
     * Validates that a string starts with a specific prefix.
     *
     * @param prefix the required prefix
     * @return validation rule
     * @see StringRules#startsWith(String)
     */
    public static ValidationRule<String> startsWith(String prefix) {
        return StringRules.startsWith(prefix);
    }

    /**
     * Validates that a string ends with a specific suffix.
     *
     * @param suffix the required suffix
     * @return validation rule
     * @see StringRules#endsWith(String)
     */
    public static ValidationRule<String> endsWith(String suffix) {
        return StringRules.endsWith(suffix);
    }

    /**
     * Validates that a string contains a specific substring.
     *
     * @param substring the required substring
     * @return validation rule
     * @see StringRules#contains(String)
     */
    public static ValidationRule<String> contains(String substring) {
        return StringRules.contains(substring);
    }

    /**
     * Validates that a string does not contain a specific substring.
     *
     * @param substring the forbidden substring
     * @return validation rule
     * @see StringRules#doesNotContain(String)
     */
    public static ValidationRule<String> doesNotContain(String substring) {
        return StringRules.doesNotContain(substring);
    }

    /**
     * Validates that a string is alphanumeric (only letters and digits).
     *
     * @return validation rule
     * @see StringRules#alphanumeric()
     */
    public static ValidationRule<String> alphanumeric() {
        return StringRules.alphanumeric();
    }

    /**
     * Validates that a string is alphabetic (only letters).
     *
     * @return validation rule
     * @see StringRules#alphabetic()
     */
    public static ValidationRule<String> alphabetic() {
        return StringRules.alphabetic();
    }

    /**
     * Validates that a string is numeric (only digits).
     *
     * @return validation rule
     * @see StringRules#numeric()
     */
    public static ValidationRule<String> numeric() {
        return StringRules.numeric();
    }

    /**
     * Validates that a string is lowercase.
     *
     * @return validation rule
     * @see StringRules#lowercase()
     */
    public static ValidationRule<String> lowercase() {
        return StringRules.lowercase();
    }

    /**
     * Validates that a string is uppercase.
     *
     * @return validation rule
     * @see StringRules#uppercase()
     */
    public static ValidationRule<String> uppercase() {
        return StringRules.uppercase();
    }

    // ==================== Numeric Rules ====================

    /**
     * Validates that a number is positive (> 0).
     *
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#positive()
     */
    public static <T extends Number> ValidationRule<T> positive() {
        return NumericRules.positive();
    }

    /**
     * Validates that a number is negative (< 0).
     *
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#negative()
     */
    public static <T extends Number> ValidationRule<T> negative() {
        return NumericRules.negative();
    }

    /**
     * Validates that a number is non-negative (>= 0).
     *
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#nonNegative()
     */
    public static <T extends Number> ValidationRule<T> nonNegative() {
        return NumericRules.nonNegative();
    }

    /**
     * Validates that a number is non-positive (<= 0).
     *
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#nonPositive()
     */
    public static <T extends Number> ValidationRule<T> nonPositive() {
        return NumericRules.nonPositive();
    }

    /**
     * Validates that a number is zero.
     *
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#zero()
     */
    public static <T extends Number> ValidationRule<T> zero() {
        return NumericRules.zero();
    }

    /**
     * Validates minimum numeric value.
     *
     * @param min minimum value (inclusive)
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#min(double)
     */
    public static <T extends Number> ValidationRule<T> min(double min) {
        return NumericRules.min(min);
    }

    /**
     * Validates maximum numeric value.
     *
     * @param max maximum value (inclusive)
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#max(double)
     */
    public static <T extends Number> ValidationRule<T> max(double max) {
        return NumericRules.max(max);
    }

    /**
     * Validates that a number is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#between(double, double)
     */
    public static <T extends Number> ValidationRule<T> between(double min, double max) {
        return NumericRules.between(min, max);
    }

    /**
     * Validates that an integer is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return validation rule
     * @see NumericRules#integerBetween(int, int)
     */
    public static ValidationRule<Integer> integerBetween(int min, int max) {
        return NumericRules.integerBetween(min, max);
    }

    /**
     * Validates that a long is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return validation rule
     * @see NumericRules#longBetween(long, long)
     */
    public static ValidationRule<Long> longBetween(long min, long max) {
        return NumericRules.longBetween(min, max);
    }

    /**
     * Validates that a value is greater than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#greaterThan(double)
     */
    public static <T extends Number> ValidationRule<T> greaterThan(double threshold) {
        return NumericRules.greaterThan(threshold);
    }

    /**
     * Validates that a value is less than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the number type
     * @return validation rule
     * @see NumericRules#lessThan(double)
     */
    public static <T extends Number> ValidationRule<T> lessThan(double threshold) {
        return NumericRules.lessThan(threshold);
    }

    /**
     * Validates that an integer is a valid port number (1-65535).
     *
     * @return validation rule
     * @see NumericRules#port()
     */
    public static ValidationRule<Integer> port() {
        return NumericRules.port();
    }

    /**
     * Validates that a number is even.
     *
     * @return validation rule
     * @see NumericRules#even()
     */
    public static ValidationRule<Integer> even() {
        return NumericRules.even();
    }

    /**
     * Validates that a number is odd.
     *
     * @return validation rule
     * @see NumericRules#odd()
     */
    public static ValidationRule<Integer> odd() {
        return NumericRules.odd();
    }

    /**
     * Validates that a number is a multiple of another number.
     *
     * @param divisor the divisor
     * @return validation rule
     * @see NumericRules#multipleOf(int)
     */
    public static ValidationRule<Integer> multipleOf(int divisor) {
        return NumericRules.multipleOf(divisor);
    }

    // ==================== File Rules ====================

    /**
     * Validates that a file or directory exists.
     *
     * @return validation rule
     * @see FileRules#exists()
     */
    public static ValidationRule<String> exists() {
        return FileRules.exists();
    }

    /**
     * Validates that a file exists.
     *
     * @return validation rule
     * @see FileRules#fileExists()
     */
    public static ValidationRule<String> fileExists() {
        return FileRules.fileExists();
    }

    /**
     * Validates that a directory exists.
     *
     * @return validation rule
     * @see FileRules#directoryExists()
     */
    public static ValidationRule<String> directoryExists() {
        return FileRules.directoryExists();
    }

    /**
     * Validates that a file is readable.
     *
     * @return validation rule
     * @see FileRules#readable()
     */
    public static ValidationRule<String> readable() {
        return FileRules.readable();
    }

    /**
     * Validates that a file is writable.
     *
     * @return validation rule
     * @see FileRules#writable()
     */
    public static ValidationRule<String> writable() {
        return FileRules.writable();
    }

    /**
     * Validates that a file is executable.
     *
     * @return validation rule
     * @see FileRules#executable()
     */
    public static ValidationRule<String> executable() {
        return FileRules.executable();
    }

    /**
     * Validates that a path is a directory.
     *
     * @return validation rule
     * @see FileRules#isDirectory()
     */
    public static ValidationRule<String> isDirectory() {
        return FileRules.isDirectory();
    }

    /**
     * Validates that a path is a regular file.
     *
     * @return validation rule
     * @see FileRules#isFile()
     */
    public static ValidationRule<String> isFile() {
        return FileRules.isFile();
    }

    /**
     * Validates that a file has a specific extension.
     *
     * @param extension the required extension (with or without leading dot)
     * @return validation rule
     * @see FileRules#hasExtension(String)
     */
    public static ValidationRule<String> hasExtension(String extension) {
        return FileRules.hasExtension(extension);
    }

    // ==================== General Rules ====================

    /**
     * Validates that a value is required (not null).
     *
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#required()
     */
    public static <T> ValidationRule<T> required() {
        return GeneralRules.required();
    }

    /**
     * Validates that a value is not null.
     *
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#notNull()
     */
    public static <T> ValidationRule<T> notNull() {
        return GeneralRules.notNull();
    }

    /**
     * Validates that a value is one of the allowed values.
     *
     * @param allowedValues collection of allowed values
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#oneOf(Collection)
     */
    public static <T> ValidationRule<T> oneOf(Collection<T> allowedValues) {
        return GeneralRules.oneOf(allowedValues);
    }

    /**
     * Validates that a value is one of the allowed values (varargs version).
     *
     * @param allowedValues allowed values
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#oneOf(Object[])
     */
    @SafeVarargs
    public static <T> ValidationRule<T> oneOf(T... allowedValues) {
        return GeneralRules.oneOf(allowedValues);
    }

    /**
     * Validates that a value is not one of the forbidden values.
     *
     * @param forbiddenValues collection of forbidden values
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#noneOf(Collection)
     */
    public static <T> ValidationRule<T> noneOf(Collection<T> forbiddenValues) {
        return GeneralRules.noneOf(forbiddenValues);
    }

    /**
     * Validates that a value is not one of the forbidden values (varargs version).
     *
     * @param forbiddenValues forbidden values
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#noneOf(Object[])
     */
    @SafeVarargs
    public static <T> ValidationRule<T> noneOf(T... forbiddenValues) {
        return GeneralRules.noneOf(forbiddenValues);
    }

    /**
     * Validates that a value equals an expected value.
     *
     * @param expectedValue the expected value
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#equalTo(Object)
     */
    public static <T> ValidationRule<T> equalTo(T expectedValue) {
        return GeneralRules.equalTo(expectedValue);
    }

    /**
     * Validates that a value does not equal a forbidden value.
     *
     * @param forbiddenValue the forbidden value
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#notEqualTo(Object)
     */
    public static <T> ValidationRule<T> notEqualTo(T forbiddenValue) {
        return GeneralRules.notEqualTo(forbiddenValue);
    }

    /**
     * Creates a custom validation rule using a predicate.
     *
     * @param predicate the validation predicate (returns true if valid)
     * @param errorMessage the error message if validation fails
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#custom(Predicate, String)
     */
    public static <T> ValidationRule<T> custom(Predicate<T> predicate, String errorMessage) {
        return GeneralRules.custom(predicate, errorMessage);
    }

    /**
     * Creates a custom validation rule using a predicate with custom error details.
     *
     * @param predicate the validation predicate (returns true if valid)
     * @param errorMessage the error message if validation fails
     * @param expectedValue description of expected value
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#custom(Predicate, String, String)
     */
    public static <T> ValidationRule<T> custom(Predicate<T> predicate, String errorMessage, String expectedValue) {
        return GeneralRules.custom(predicate, errorMessage, expectedValue);
    }

    /**
     * Validates that a value satisfies a custom predicate with access to context.
     *
     * @param contextPredicate the validation predicate with context access
     * @param errorMessage the error message if validation fails
     * @param <T> the value type
     * @return validation rule
     * @see GeneralRules#customWithContext(BiPredicate, String)
     */
    public static <T> ValidationRule<T> customWithContext(
            BiPredicate<T, PropertyContext> contextPredicate,
            String errorMessage) {
        return GeneralRules.customWithContext(contextPredicate, errorMessage);
    }

    // ==================== Composite Rules ====================

    /**
     * Creates a composite rule that passes only if ALL provided rules pass (AND logic).
     *
     * <p>The composite rule short-circuits on the first failure and returns that error.
     * All rules must pass for the composite rule to pass.
     *
     * <p>Example:
     * <pre>
     * ValidationRule&lt;String&gt; strictRule = Rules.allOf(
     *     Rules.notBlank(),
     *     Rules.minLength(3),
     *     Rules.maxLength(50)
     * );
     * </pre>
     *
     * @param rules the rules to combine with AND logic
     * @param <T> the value type
     * @return composite validation rule
     * @throws IllegalArgumentException if no rules are provided
     */
    @SafeVarargs
    public static <T> ValidationRule<T> allOf(ValidationRule<T>... rules) {
        if (rules == null || rules.length == 0) {
            throw new IllegalArgumentException("At least one rule is required for allOf()");
        }

        return (propertyName, value, context) -> {
            for (ValidationRule<T> rule : rules) {
                ValidationResult result = rule.validate(propertyName, value, context);
                if (!result.isValid()) {
                    return result;
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Creates a composite rule that passes if ANY provided rule passes (OR logic).
     *
     * <p>The composite rule tries all rules and passes if at least one succeeds.
     * If all rules fail, it returns a validation result containing all errors.
     *
     * <p>Example:
     * <pre>
     * ValidationRule&lt;String&gt; flexibleRule = Rules.anyOf(
     *     Rules.allOf(Rules.notBlank(), Rules.maxLength(50)),
     *     Rules.allOf(Rules.email(), Rules.endsWith("@company.com"))
     * );
     * </pre>
     *
     * @param rules the rules to combine with OR logic
     * @param <T> the value type
     * @return composite validation rule
     * @throws IllegalArgumentException if no rules are provided
     */
    @SafeVarargs
    public static <T> ValidationRule<T> anyOf(ValidationRule<T>... rules) {
        if (rules == null || rules.length == 0) {
            throw new IllegalArgumentException("At least one rule is required for anyOf()");
        }

        return (propertyName, value, context) -> {
            List<ValidationError> allErrors = new ArrayList<>();

            for (ValidationRule<T> rule : rules) {
                ValidationResult result = rule.validate(propertyName, value, context);
                if (result.isValid()) {
                    return ValidationResult.success();
                }
                allErrors.addAll(result.getErrors());
            }

            return ValidationResult.failure(allErrors);
        };
    }
}
