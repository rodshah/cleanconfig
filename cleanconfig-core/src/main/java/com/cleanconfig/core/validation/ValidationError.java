package com.cleanconfig.core.validation;

import java.util.Objects;

/**
 * Immutable validation error with builder for construction.
 *
 * <p>A validation error contains:
 * <ul>
 *   <li>Property name that failed validation</li>
 *   <li>Error message describing what went wrong</li>
 *   <li>Optional actual value that was validated</li>
 *   <li>Optional expected value or constraint</li>
 *   <li>Optional error code for programmatic handling</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * ValidationError error = ValidationError.builder()
 *     .propertyName("server.port")
 *     .errorMessage("Port must be between 1024 and 65535")
 *     .actualValue("80")
 *     .expectedValue("1024-65535")
 *     .errorCode("PORT_OUT_OF_RANGE")
 *     .build();
 * </pre>
 *
 * @since 0.1.0
 */
public final class ValidationError {

    private final String propertyName;
    private final String errorMessage;
    private final String actualValue;
    private final String expectedValue;
    private final String errorCode;

    private ValidationError(Builder builder) {
        this.propertyName = Objects.requireNonNull(builder.propertyName, "propertyName cannot be null");
        this.errorMessage = Objects.requireNonNull(builder.errorMessage, "errorMessage cannot be null");
        this.actualValue = builder.actualValue;
        this.expectedValue = builder.expectedValue;
        this.errorCode = builder.errorCode;
    }

    /**
     * Creates a new builder for ValidationError.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the name of the property that failed validation.
     *
     * @return property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the error message.
     *
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the actual value that failed validation.
     *
     * @return actual value, or null if not set
     */
    public String getActualValue() {
        return actualValue;
    }

    /**
     * Gets the expected value or constraint.
     *
     * @return expected value, or null if not set
     */
    public String getExpectedValue() {
        return expectedValue;
    }

    /**
     * Gets the error code for programmatic handling.
     *
     * @return error code, or null if not set
     */
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationError that = (ValidationError) o;
        return propertyName.equals(that.propertyName) &&
                errorMessage.equals(that.errorMessage) &&
                Objects.equals(actualValue, that.actualValue) &&
                Objects.equals(expectedValue, that.expectedValue) &&
                Objects.equals(errorCode, that.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, errorMessage, actualValue, expectedValue, errorCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationError{property='").append(propertyName).append("'");
        sb.append(", message='").append(errorMessage).append("'");
        if (actualValue != null) {
            sb.append(", actual='").append(actualValue).append("'");
        }
        if (expectedValue != null) {
            sb.append(", expected='").append(expectedValue).append("'");
        }
        if (errorCode != null) {
            sb.append(", code='").append(errorCode).append("'");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Builder for ValidationError.
     */
    public static final class Builder {
        private String propertyName;
        private String errorMessage;
        private String actualValue;
        private String expectedValue;
        private String errorCode;

        private Builder() {
        }

        /**
         * Sets the property name.
         *
         * @param propertyName the property name (required)
         * @return this builder
         */
        public Builder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        /**
         * Sets the error message.
         *
         * @param errorMessage the error message (required)
         * @return this builder
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Sets the actual value.
         *
         * @param actualValue the actual value (optional)
         * @return this builder
         */
        public Builder actualValue(String actualValue) {
            this.actualValue = actualValue;
            return this;
        }

        /**
         * Sets the expected value.
         *
         * @param expectedValue the expected value (optional)
         * @return this builder
         */
        public Builder expectedValue(String expectedValue) {
            this.expectedValue = expectedValue;
            return this;
        }

        /**
         * Sets the error code.
         *
         * @param errorCode the error code (optional)
         * @return this builder
         */
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Builds the ValidationError.
         *
         * @return the validation error
         * @throws NullPointerException if propertyName or errorMessage is null
         */
        public ValidationError build() {
            return new ValidationError(this);
        }
    }
}
