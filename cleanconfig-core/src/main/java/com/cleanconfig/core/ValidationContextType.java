package com.cleanconfig.core;

/**
 * Defines different contexts in which properties are validated.
 *
 * <p>Different contexts may have different validation requirements. For example:
 * <ul>
 *   <li>STARTUP: Stricter validation for initial configuration</li>
 *   <li>RUNTIME_OVERRIDE: More lenient validation for runtime changes</li>
 *   <li>PERSISTED: Validation for stored configurations</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * PropertyContext context = DefaultPropertyContext.builder()
 *     .properties(props)
 *     .contextType(ValidationContextType.RUNTIME_OVERRIDE)
 *     .build();
 * </pre>
 *
 * @since 0.1.0
 */
public enum ValidationContextType {

    /**
     * Application startup validation.
     *
     * <p>This is the strictest validation level, ensuring all required
     * properties are present and valid before the application starts.
     */
    STARTUP,

    /**
     * Runtime property override validation.
     *
     * <p>Used when properties are changed at runtime. May be more lenient
     * than STARTUP validation to avoid breaking running systems.
     */
    RUNTIME_OVERRIDE,

    /**
     * Persisted configuration validation.
     *
     * <p>Used when loading or saving property configurations to storage.
     */
    PERSISTED,

    /**
     * Testing context validation.
     *
     * <p>Used in test environments. May allow invalid values for testing
     * error handling scenarios.
     */
    TESTING
}
