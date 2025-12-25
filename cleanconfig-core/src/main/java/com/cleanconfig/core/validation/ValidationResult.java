package com.cleanconfig.core.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result of property validation.
 *
 * <p>A validation result is either successful (valid) or failed (invalid)
 * with one or more validation errors.
 *
 * <p>Example usage:
 * <pre>
 * ValidationResult result = validator.validate(properties);
 * if (result.isValid()) {
 *     System.out.println("All properties valid");
 * } else {
 *     result.getErrors().forEach(error -&gt;
 *         System.err.println(error.getPropertyName() + ": " + error.getErrorMessage())
 *     );
 * }
 * </pre>
 *
 * @since 0.1.0
 */
public final class ValidationResult {

    private static final ValidationResult SUCCESS = new ValidationResult(Collections.emptyList());

    private final List<ValidationError> errors;

    private ValidationResult(List<ValidationError> errors) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    /**
     * Creates a successful validation result.
     *
     * @return a successful validation result
     */
    public static ValidationResult success() {
        return SUCCESS;
    }

    /**
     * Creates a failed validation result with a single error.
     *
     * @param error the validation error
     * @return a failed validation result
     */
    public static ValidationResult failure(ValidationError error) {
        Objects.requireNonNull(error, "error cannot be null");
        return new ValidationResult(Collections.singletonList(error));
    }

    /**
     * Creates a failed validation result with multiple errors.
     *
     * @param errors the validation errors
     * @return a failed validation result
     */
    public static ValidationResult failure(List<ValidationError> errors) {
        Objects.requireNonNull(errors, "errors cannot be null");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("errors cannot be empty for a failure result");
        }
        return new ValidationResult(errors);
    }

    /**
     * Checks if validation was successful.
     *
     * @return true if validation passed, false otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Gets the list of validation errors.
     *
     * @return immutable list of errors (empty if validation succeeded)
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Gets the number of validation errors.
     *
     * @return error count (0 if validation succeeded)
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Combines this result with another result.
     *
     * <p>The combined result is successful only if both results are successful.
     * Otherwise, all errors from both results are included.
     *
     * @param other the other validation result
     * @return the combined result
     */
    public ValidationResult combine(ValidationResult other) {
        Objects.requireNonNull(other, "other result cannot be null");

        if (this.isValid() && other.isValid()) {
            return SUCCESS;
        }

        List<ValidationError> combined = new ArrayList<>();
        combined.addAll(this.errors);
        combined.addAll(other.errors);
        return new ValidationResult(combined);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return errors.equals(that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errors);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "ValidationResult{valid}";
        }
        return "ValidationResult{errors=" + errors.size() + "}";
    }
}
