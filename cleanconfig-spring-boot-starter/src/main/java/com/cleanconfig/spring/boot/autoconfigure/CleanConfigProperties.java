package com.cleanconfig.spring.boot.autoconfigure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for CleanConfig Spring Boot integration.
 *
 * <p>Example configuration in application.properties:
 * <pre>
 * cleanconfig.enabled=true
 * cleanconfig.validation.fail-on-error=true
 * cleanconfig.validation.log-warnings=true
 * </pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "cleanconfig")
public class CleanConfigProperties {

    /**
     * Whether to enable CleanConfig auto-configuration.
     */
    private boolean enabled = true;

    /**
     * Validation configuration.
     */
    private final Validation validation = new Validation();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring Boot @ConfigurationProperties requires mutable nested objects for property binding"
    )
    public Validation getValidation() {
        return validation;
    }

    /**
     * Validation configuration properties.
     */
    public static class Validation {

        /**
         * Whether to fail application startup on validation errors.
         * If false, validation errors are logged as warnings.
         */
        private boolean failOnError = true;

        /**
         * Whether to log validation warnings.
         */
        private boolean logWarnings = true;

        public boolean isFailOnError() {
            return failOnError;
        }

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        public boolean isLogWarnings() {
            return logWarnings;
        }

        public void setLogWarnings(boolean logWarnings) {
            this.logWarnings = logWarnings;
        }
    }
}
