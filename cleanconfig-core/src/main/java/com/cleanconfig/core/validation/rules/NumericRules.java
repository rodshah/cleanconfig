package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;

/**
 * Validation rules for numeric values.
 *
 * <p>Provides rules for numeric validation including range checks, sign checks,
 * and specialized validations like port numbers.
 *
 * <p>Example usage:
 * <pre>
 * ValidationRule&lt;Integer&gt; portRule = NumericRules.port();
 * ValidationRule&lt;Double&gt; percentRule = NumericRules.between(0.0, 100.0);
 * ValidationRule&lt;Integer&gt; positiveRule = NumericRules.positive();
 * </pre>
 *
 * @since 0.1.0
 */
public final class NumericRules {

    private NumericRules() {
        // Utility class
    }

    /**
     * Validates that a number is positive (> 0).
     *
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> positive() {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() <= 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be positive")
                                .actualValue(String.valueOf(value))
                                .expectedValue("> 0")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is negative (< 0).
     *
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> negative() {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() >= 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be negative")
                                .actualValue(String.valueOf(value))
                                .expectedValue("< 0")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is non-negative (>= 0).
     *
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> nonNegative() {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() < 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be non-negative")
                                .actualValue(String.valueOf(value))
                                .expectedValue(">= 0")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is non-positive (<= 0).
     *
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> nonPositive() {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() > 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be non-positive")
                                .actualValue(String.valueOf(value))
                                .expectedValue("<= 0")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is zero.
     *
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> zero() {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() != 0.0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be zero")
                                .actualValue(String.valueOf(value))
                                .expectedValue("0")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates minimum numeric value.
     *
     * @param min minimum value (inclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> min(double min) {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() < min) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be at least " + min)
                                .actualValue(String.valueOf(value))
                                .expectedValue(">= " + min)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates maximum numeric value.
     *
     * @param max maximum value (inclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> max(double max) {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() > max) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must not exceed " + max)
                                .actualValue(String.valueOf(value))
                                .expectedValue("<= " + max)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> between(double min, double max) {
        return (name, value, context) -> {
            if (value != null) {
                double d = value.doubleValue();
                if (d < min || d > max) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Value must be between " + min + " and " + max)
                                    .actualValue(String.valueOf(value))
                                    .expectedValue("[" + min + ", " + max + "]")
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that an integer is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return validation rule
     */
    public static ValidationRule<Integer> integerBetween(int min, int max) {
        return (name, value, context) -> {
            if (value != null && (value < min || value > max)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be between " + min + " and " + max)
                                .actualValue(String.valueOf(value))
                                .expectedValue("[" + min + ", " + max + "]")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a long is within a range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return validation rule
     */
    public static ValidationRule<Long> longBetween(long min, long max) {
        return (name, value, context) -> {
            if (value != null && (value < min || value > max)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be between " + min + " and " + max)
                                .actualValue(String.valueOf(value))
                                .expectedValue("[" + min + ", " + max + "]")
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is greater than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> greaterThan(double threshold) {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() <= threshold) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be greater than " + threshold)
                                .actualValue(String.valueOf(value))
                                .expectedValue("> " + threshold)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is less than a threshold.
     *
     * @param threshold the threshold value (exclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> lessThan(double threshold) {
        return (name, value, context) -> {
            if (value != null && value.doubleValue() >= threshold) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be less than " + threshold)
                                .actualValue(String.valueOf(value))
                                .expectedValue("< " + threshold)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a value is greater than or equal to a threshold.
     *
     * @param threshold the threshold value (inclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> greaterThanOrEqualTo(double threshold) {
        return min(threshold);
    }

    /**
     * Validates that a value is less than or equal to a threshold.
     *
     * @param threshold the threshold value (inclusive)
     * @param <T> the number type
     * @return validation rule
     */
    public static <T extends Number> ValidationRule<T> lessThanOrEqualTo(double threshold) {
        return max(threshold);
    }

    /**
     * Validates that an integer is a valid port number (1-65535).
     *
     * @return validation rule
     */
    public static ValidationRule<Integer> port() {
        return integerBetween(1, 65535);
    }

    /**
     * Validates that a number is even.
     *
     * @return validation rule
     */
    public static ValidationRule<Integer> even() {
        return (name, value, context) -> {
            if (value != null && value % 2 != 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be even")
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is odd.
     *
     * @return validation rule
     */
    public static ValidationRule<Integer> odd() {
        return (name, value, context) -> {
            if (value != null && value % 2 == 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be odd")
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a number is a multiple of another number.
     *
     * @param divisor the divisor
     * @return validation rule
     */
    public static ValidationRule<Integer> multipleOf(int divisor) {
        return (name, value, context) -> {
            if (value != null && value % divisor != 0) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("Value must be a multiple of " + divisor)
                                .actualValue(String.valueOf(value))
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }
}
