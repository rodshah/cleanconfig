package com.cleanconfig.core.converter;

import java.util.Optional;

/**
 * Functional interface for converting string values to typed values.
 *
 * <p>Type converters enable automatic conversion of string property values
 * to their desired types with proper error handling.
 *
 * <p>Example usage:
 * <pre>
 * TypeConverter&lt;Integer&gt; intConverter = value -&gt; {
 *     try {
 *         return Optional.of(Integer.parseInt(value));
 *     } catch (NumberFormatException e) {
 *         return Optional.empty();
 *     }
 * };
 *
 * Optional&lt;Integer&gt; result = intConverter.convert("42");
 * </pre>
 *
 * @param <T> the target type
 * @since 0.1.0
 */
@FunctionalInterface
public interface TypeConverter<T> {

    /**
     * Converts a string value to the target type.
     *
     * @param value the string value to convert (never null)
     * @return optional containing the converted value, or empty if conversion failed
     */
    Optional<T> convert(String value);

    /**
     * Gets the target type class.
     *
     * <p>Default implementation throws UnsupportedOperationException.
     * Implementations should override this method to return the actual type.
     *
     * @return the target type class
     */
    default Class<T> getTargetType() {
        throw new UnsupportedOperationException("getTargetType() not implemented");
    }
}
