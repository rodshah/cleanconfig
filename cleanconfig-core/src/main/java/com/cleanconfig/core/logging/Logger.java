package com.cleanconfig.core.logging;

/**
 * Logging abstraction that delegates to underlying logging implementations.
 *
 * <p>This interface provides a simple logging API without forcing any specific
 * logging framework dependency. Implementations can delegate to SLF4J, JUL,
 * or custom logging systems.
 *
 * <p>Example usage:
 * <pre>
 * Logger log = LoggerFactory.getLogger(MyClass.class);
 * log.info("Processing property: {}", propertyName);
 * log.error("Validation failed", exception);
 * </pre>
 *
 * @since 0.1.0
 */
public interface Logger {

    /**
     * Logs a debug message.
     *
     * @param message the message to log
     */
    void debug(String message);

    /**
     * Logs a debug message with arguments.
     *
     * <p>Arguments are formatted using {@code {}} placeholders.
     *
     * @param message the message template with {} placeholders
     * @param args the arguments to format into the message
     */
    void debug(String message, Object... args);

    /**
     * Logs an info message.
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs an info message with arguments.
     *
     * <p>Arguments are formatted using {@code {}} placeholders.
     *
     * @param message the message template with {} placeholders
     * @param args the arguments to format into the message
     */
    void info(String message, Object... args);

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Logs a warning message with arguments.
     *
     * <p>Arguments are formatted using {@code {}} placeholders.
     *
     * @param message the message template with {} placeholders
     * @param args the arguments to format into the message
     */
    void warn(String message, Object... args);

    /**
     * Logs a warning message with an exception.
     *
     * @param message the message to log
     * @param throwable the exception to log
     */
    void warn(String message, Throwable throwable);

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    void error(String message);

    /**
     * Logs an error message with arguments.
     *
     * <p>Arguments are formatted using {@code {}} placeholders.
     *
     * @param message the message template with {} placeholders
     * @param args the arguments to format into the message
     */
    void error(String message, Object... args);

    /**
     * Logs an error message with an exception.
     *
     * @param message the message to log
     * @param throwable the exception to log
     */
    void error(String message, Throwable throwable);

    /**
     * Checks if debug logging is enabled.
     *
     * @return true if debug is enabled
     */
    boolean isDebugEnabled();

    /**
     * Checks if info logging is enabled.
     *
     * @return true if info is enabled
     */
    boolean isInfoEnabled();
}
