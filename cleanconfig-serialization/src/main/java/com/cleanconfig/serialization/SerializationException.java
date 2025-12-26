package com.cleanconfig.serialization;

/**
 * Exception thrown when property serialization or deserialization fails.
 *
 * @since 0.1.0
 */
public class SerializationException extends Exception {

    /**
     * Constructs a new serialization exception with the specified detail message.
     *
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new serialization exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
