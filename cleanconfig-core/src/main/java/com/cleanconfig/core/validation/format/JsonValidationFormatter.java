package com.cleanconfig.core.validation.format;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;

import java.util.Objects;

/**
 * Formats validation results as JSON for machine-readable output.
 *
 * <p>Output format (success):
 * <pre>
 * {
 *   "valid": true,
 *   "errorCount": 0,
 *   "errors": []
 * }
 * </pre>
 *
 * <p>Output format (failure):
 * <pre>
 * {
 *   "valid": false,
 *   "errorCount": 2,
 *   "errors": [
 *     {
 *       "propertyName": "server.port",
 *       "errorMessage": "Port must be between 1024 and 65535",
 *       "actualValue": "80",
 *       "expectedValue": "1024-65535",
 *       "errorCode": "PORT_OUT_OF_RANGE",
 *       "suggestion": "Use a port >= 1024 to avoid requiring root privileges"
 *     },
 *     {
 *       "propertyName": "db.url",
 *       "errorMessage": "Value is not a valid URL",
 *       "actualValue": "invalid-url"
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>This formatter produces valid JSON without requiring external dependencies.
 * Fields with null values are omitted from the output.
 *
 * @since 0.1.0
 */
public class JsonValidationFormatter implements ValidationFormatter {

    private static final String INDENT = "  ";

    /**
     * Formats a validation result as JSON.
     *
     * @param result the validation result to format
     * @return formatted JSON string
     * @throws NullPointerException if result is null
     */
    @Override
    public String format(ValidationResult result) {
        Objects.requireNonNull(result, "result cannot be null");

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(INDENT).append("\"valid\": ").append(result.isValid()).append(",\n");
        sb.append(INDENT).append("\"errorCount\": ").append(result.getErrorCount()).append(",\n");

        // Handle empty errors array cleanly on one line
        if (result.getErrors().isEmpty()) {
            sb.append(INDENT).append("\"errors\": []\n");
        } else {
            sb.append(INDENT).append("\"errors\": [\n");
            int errorIndex = 0;
            for (ValidationError error : result.getErrors()) {
                if (errorIndex > 0) {
                    sb.append(",\n");
                }
                formatError(sb, error);
                errorIndex++;
            }
            sb.append("\n");
            sb.append(INDENT).append("]\n");
        }

        sb.append("}");

        return sb.toString();
    }

    private void formatError(StringBuilder sb, ValidationError error) {
        String doubleIndent = INDENT + INDENT;
        sb.append(doubleIndent).append("{\n");

        // Required fields
        sb.append(doubleIndent).append(INDENT)
          .append("\"propertyName\": ")
          .append(escapeJson(error.getPropertyName()))
          .append(",\n");

        sb.append(doubleIndent).append(INDENT)
          .append("\"errorMessage\": ")
          .append(escapeJson(error.getErrorMessage()));

        // Optional fields (only include if not null)
        if (error.getActualValue() != null) {
            sb.append(",\n");
            sb.append(doubleIndent).append(INDENT)
              .append("\"actualValue\": ")
              .append(escapeJson(error.getActualValue()));
        }

        if (error.getExpectedValue() != null) {
            sb.append(",\n");
            sb.append(doubleIndent).append(INDENT)
              .append("\"expectedValue\": ")
              .append(escapeJson(error.getExpectedValue()));
        }

        if (error.getErrorCode() != null) {
            sb.append(",\n");
            sb.append(doubleIndent).append(INDENT)
              .append("\"errorCode\": ")
              .append(escapeJson(error.getErrorCode()));
        }

        if (error.getSuggestion() != null) {
            sb.append(",\n");
            sb.append(doubleIndent).append(INDENT)
              .append("\"suggestion\": ")
              .append(escapeJson(error.getSuggestion()));
        }

        sb.append("\n");
        sb.append(doubleIndent).append("}");
    }

    /**
     * Escapes a string for JSON output.
     * Handles special characters: quotes, backslashes, newlines, tabs, etc.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('"');

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    // Control characters (0x00-0x1F) must be escaped as unicode
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }

        sb.append('"');
        return sb.toString();
    }
}
