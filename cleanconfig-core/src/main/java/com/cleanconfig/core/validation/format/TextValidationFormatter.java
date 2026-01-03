package com.cleanconfig.core.validation.format;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.Objects;

/**
 * Formats validation results as human-readable text for console/terminal output.
 *
 * <p>Output format:
 * <pre>
 * Validation failed with 2 errors:
 *
 * Error 1: server.port
 *   Message: Port must be between 1024 and 65535
 *   Actual: 80
 *   Expected: 1024-65535
 *   Suggestion: Use a port >= 1024 to avoid requiring root privileges
 *
 * Error 2: db.url
 *   Message: Value is not a valid URL
 *   Actual: invalid-url
 * </pre>
 *
 * <p>For successful validation:
 * <pre>
 * Validation passed: 0 errors
 * </pre>
 *
 * @since 0.1.0
 */
public class TextValidationFormatter implements ValidationFormatter {

    private static final String INDENT = "  ";
    private static final String SUCCESS_MESSAGE = "Validation passed: 0 errors";
    private static final String FAILURE_PREFIX = "Validation failed with ";

    /**
     * Formats a validation result as human-readable text.
     *
     * @param result the validation result to format
     * @return formatted text representation
     * @throws NullPointerException if result is null
     */
    @Override
    public String format(ValidationResult result) {
        Objects.requireNonNull(result, "result cannot be null");

        if (result.isValid()) {
            return SUCCESS_MESSAGE;
        }

        StringBuilder sb = new StringBuilder();
        int errorCount = result.getErrorCount();

        sb.append(FAILURE_PREFIX)
          .append(errorCount)
          .append(errorCount == 1 ? " error:" : " errors:")
          .append("\n\n");

        int errorNumber = 1;
        for (ValidationError error : result.getErrors()) {
            formatError(sb, errorNumber++, error);
            sb.append("\n");
        }

        // Remove trailing newline
        return sb.toString().trim();
    }

    private void formatError(StringBuilder sb, int errorNumber, ValidationError error) {
        sb.append("Error ").append(errorNumber).append(": ").append(error.getPropertyName()).append("\n");
        sb.append(INDENT).append("Message: ").append(error.getErrorMessage()).append("\n");

        if (error.getActualValue() != null) {
            sb.append(INDENT).append("Actual: ").append(error.getActualValue()).append("\n");
        }

        if (error.getExpectedValue() != null) {
            sb.append(INDENT).append("Expected: ").append(error.getExpectedValue()).append("\n");
        }

        if (error.getErrorCode() != null) {
            sb.append(INDENT).append("Code: ").append(error.getErrorCode()).append("\n");
        }

        if (error.getSuggestion() != null) {
            sb.append(INDENT).append("Suggestion: ").append(error.getSuggestion()).append("\n");
        }
    }
}
