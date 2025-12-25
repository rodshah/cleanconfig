package com.cleanconfig.core.logging;

/**
 * Provider interface for creating logger instances.
 *
 * <p>Implementations of this interface integrate with specific logging frameworks
 * like SLF4J, JUL (java.util.logging), or custom logging systems.
 *
 * <p>The {@link LoggerFactory} automatically detects available logging frameworks
 * and selects an appropriate provider. Users can also explicitly set a provider:
 * <pre>
 * LoggerFactory.setLoggerProvider(new CustomLoggerProvider());
 * </pre>
 *
 * @since 0.1.0
 */
public interface LoggerProvider {

    /**
     * Creates a logger instance for the specified class.
     *
     * @param clazz the class for which to create a logger
     * @return a logger instance
     */
    Logger getLogger(Class<?> clazz);

    /**
     * Creates a logger instance with the specified name.
     *
     * @param name the logger name
     * @return a logger instance
     */
    Logger getLogger(String name);

    /**
     * Returns the name of this logging provider.
     *
     * <p>Used for diagnostics and debugging.
     *
     * @return the provider name (e.g., "SLF4J", "JUL", "No-Op")
     */
    String getProviderName();

    /**
     * Checks if this provider is available in the current environment.
     *
     * <p>For example, an SLF4J provider would check if the SLF4J classes
     * are available on the classpath.
     *
     * @return true if this provider can be used
     */
    boolean isAvailable();
}
