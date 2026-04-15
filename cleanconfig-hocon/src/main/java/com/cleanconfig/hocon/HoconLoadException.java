package com.cleanconfig.hocon;

/**
 * Exception thrown when loading or parsing HOCON configuration fails.
 *
 * <p>This is an unchecked exception that wraps underlying parse or I/O failures
 * from the Typesafe Config library, providing clear error messages to callers.</p>
 *
 * @since 0.1.0
 */
public class HoconLoadException extends RuntimeException {

    /**
     * Constructs a new HOCON load exception with the specified detail message.
     *
     * @param message the detail message describing what went wrong
     */
    public HoconLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new HOCON load exception with the specified detail message and cause.
     *
     * @param message the detail message describing what went wrong
     * @param cause the underlying cause of the failure
     */
    public HoconLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
