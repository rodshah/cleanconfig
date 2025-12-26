package com.cleanconfig.core.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for type converters with built-in converters.
 *
 * <p>The registry provides type conversion for common Java types and allows
 * registration of custom converters.
 *
 * <p>Built-in converters:
 * <ul>
 *   <li>String</li>
 *   <li>Integer, Long, Double, Float, Short, Byte</li>
 *   <li>Boolean</li>
 *   <li>BigDecimal, BigInteger</li>
 *   <li>URL, URI</li>
 *   <li>Path</li>
 *   <li>Duration (ISO-8601 format, e.g., "PT30S", "PT5M")</li>
 *   <li>Instant (ISO-8601 format)</li>
 *   <li>LocalDate, LocalDateTime (ISO-8601 format)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * TypeConverterRegistry registry = TypeConverterRegistry.getInstance();
 *
 * // Use built-in converter
 * Optional&lt;Integer&gt; port = registry.convert("8080", Integer.class);
 *
 * // Register custom converter
 * registry.register(MyType.class, value -&gt; ...);
 * </pre>
 *
 * @since 0.1.0
 */
public final class TypeConverterRegistry {

    private static final TypeConverterRegistry INSTANCE = new TypeConverterRegistry();

    private final Map<Class<?>, TypeConverter<?>> converters = new ConcurrentHashMap<>();

    private TypeConverterRegistry() {
        registerBuiltInConverters();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the registry instance
     */
    public static TypeConverterRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a type converter.
     *
     * @param type the target type
     * @param converter the converter
     * @param <T> the type parameter
     */
    public <T> void register(Class<T> type, TypeConverter<T> converter) {
        converters.put(type, converter);
    }

    /**
     * Converts a string value to the target type.
     *
     * @param value the string value
     * @param targetType the target type
     * @param <T> the type parameter
     * @return optional containing the converted value, or empty if conversion failed or no converter registered
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> convert(String value, Class<T> targetType) {
        if (value == null) {
            return Optional.empty();
        }

        TypeConverter<?> converter = converters.get(targetType);
        if (converter == null) {
            return Optional.empty();
        }

        return (Optional<T>) converter.convert(value);
    }

    /**
     * Checks if a converter is registered for the given type.
     *
     * @param type the type to check
     * @return true if a converter is registered
     */
    public boolean hasConverter(Class<?> type) {
        return converters.containsKey(type);
    }

    /**
     * Registers all built-in converters.
     */
    private void registerBuiltInConverters() {
        // String (identity)
        register(String.class, Optional::of);

        // Integer
        register(Integer.class, value -> {
            try {
                return Optional.of(Integer.parseInt(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Long
        register(Long.class, value -> {
            try {
                return Optional.of(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Double
        register(Double.class, value -> {
            try {
                return Optional.of(Double.parseDouble(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Float
        register(Float.class, value -> {
            try {
                return Optional.of(Float.parseFloat(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Short
        register(Short.class, value -> {
            try {
                return Optional.of(Short.parseShort(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Byte
        register(Byte.class, value -> {
            try {
                return Optional.of(Byte.parseByte(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Boolean
        register(Boolean.class, value -> {
            String trimmed = value.trim().toLowerCase();
            if ("true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed)) {
                return Optional.of(Boolean.TRUE);
            } else if ("false".equals(trimmed) || "no".equals(trimmed) || "0".equals(trimmed)) {
                return Optional.of(Boolean.FALSE);
            }
            return Optional.empty();
        });

        // BigDecimal
        register(BigDecimal.class, value -> {
            try {
                return Optional.of(new BigDecimal(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // BigInteger
        register(BigInteger.class, value -> {
            try {
                return Optional.of(new BigInteger(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // URL
        register(URL.class, value -> {
            try {
                return Optional.of(new URL(value.trim()));
            } catch (Exception e) {
                return Optional.empty();
            }
        });

        // URI
        register(URI.class, value -> {
            try {
                return Optional.of(new URI(value.trim()));
            } catch (Exception e) {
                return Optional.empty();
            }
        });

        // Path
        register(Path.class, value -> {
            try {
                return Optional.of(Paths.get(value.trim()));
            } catch (Exception e) {
                return Optional.empty();
            }
        });

        // Duration (ISO-8601, e.g., PT30S, PT5M, PT2H)
        register(Duration.class, value -> {
            try {
                return Optional.of(Duration.parse(value.trim()));
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        });

        // Instant (ISO-8601)
        register(Instant.class, value -> {
            try {
                return Optional.of(Instant.parse(value.trim()));
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        });

        // LocalDate (ISO-8601, e.g., 2024-12-25)
        register(LocalDate.class, value -> {
            try {
                return Optional.of(LocalDate.parse(value.trim()));
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        });

        // LocalDateTime (ISO-8601, e.g., 2024-12-25T15:30:00)
        register(LocalDateTime.class, value -> {
            try {
                return Optional.of(LocalDateTime.parse(value.trim()));
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        });
    }
}
