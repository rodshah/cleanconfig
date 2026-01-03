package com.cleanconfig.core.validation.format;

import com.cleanconfig.core.validation.ValidationResult;

/**
 * Interface for formatting validation results into different output formats.
 *
 * <p>Implementations can format validation results for:
 * <ul>
 *   <li>Human-readable text (console output)</li>
 *   <li>Machine-readable JSON (tooling, APIs)</li>
 *   <li>Structured reports (HTML, Markdown)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * ValidationResult result = validator.validate(properties);
 *
 * // Format for console
 * ValidationFormatter textFormatter = new TextValidationFormatter();
 * System.out.println(textFormatter.format(result));
 *
 * // Format for API response
 * ValidationFormatter jsonFormatter = new JsonValidationFormatter();
 * String json = jsonFormatter.format(result);
 * </pre>
 *
 * @since 0.1.0
 * @see TextValidationFormatter
 * @see JsonValidationFormatter
 */
public interface ValidationFormatter {

    /**
     * Formats a validation result into a string representation.
     *
     * @param result the validation result to format
     * @return formatted string representation
     * @throws NullPointerException if result is null
     */
    String format(ValidationResult result);
}
