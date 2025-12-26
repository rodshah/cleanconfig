package com.cleanconfig.core.converter;

import org.junit.Before;
import org.junit.Test;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeConverterRegistry}.
 */
public class TypeConverterRegistryTest {

    private TypeConverterRegistry registry;

    @Before
    public void setUp() {
        registry = TypeConverterRegistry.getInstance();
    }

    // String converter tests
    @Test
    public void convert_String_ReturnsValue() {
        Optional<String> result = registry.convert("hello", String.class);
        assertThat(result).hasValue("hello");
    }

    // Integer converter tests
    @Test
    public void convert_Integer_ValidInput_ReturnsValue() {
        assertThat(registry.convert("42", Integer.class)).hasValue(42);
        assertThat(registry.convert("-100", Integer.class)).hasValue(-100);
        assertThat(registry.convert("  123  ", Integer.class)).hasValue(123);
    }

    @Test
    public void convert_Integer_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("abc", Integer.class)).isEmpty();
        assertThat(registry.convert("12.34", Integer.class)).isEmpty();
        assertThat(registry.convert("", Integer.class)).isEmpty();
    }

    // Long converter tests
    @Test
    public void convert_Long_ValidInput_ReturnsValue() {
        assertThat(registry.convert("9223372036854775807", Long.class))
                .hasValue(9223372036854775807L);
        assertThat(registry.convert("-999", Long.class)).hasValue(-999L);
    }

    @Test
    public void convert_Long_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("not a number", Long.class)).isEmpty();
    }

    // Double converter tests
    @Test
    public void convert_Double_ValidInput_ReturnsValue() {
        assertThat(registry.convert("7.89", Double.class)).hasValue(7.89);
        assertThat(registry.convert("-0.5", Double.class)).hasValue(-0.5);
        assertThat(registry.convert("1.23e10", Double.class)).hasValue(1.23e10);
    }

    @Test
    public void convert_Double_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("not a double", Double.class)).isEmpty();
    }

    // Float converter tests
    @Test
    public void convert_Float_ValidInput_ReturnsValue() {
        assertThat(registry.convert("2.71", Float.class)).hasValue(2.71f);
        assertThat(registry.convert("-1.5", Float.class)).hasValue(-1.5f);
    }

    @Test
    public void convert_Float_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("invalid", Float.class)).isEmpty();
    }

    // Short converter tests
    @Test
    public void convert_Short_ValidInput_ReturnsValue() {
        assertThat(registry.convert("32767", Short.class)).hasValue((short) 32767);
        assertThat(registry.convert("-100", Short.class)).hasValue((short) -100);
    }

    @Test
    public void convert_Short_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("99999", Short.class)).isEmpty();
    }

    // Byte converter tests
    @Test
    public void convert_Byte_ValidInput_ReturnsValue() {
        assertThat(registry.convert("127", Byte.class)).hasValue((byte) 127);
        assertThat(registry.convert("-128", Byte.class)).hasValue((byte) -128);
    }

    @Test
    public void convert_Byte_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("999", Byte.class)).isEmpty();
    }

    // Boolean converter tests
    @Test
    public void convert_Boolean_TrueValues_ReturnsTrue() {
        assertThat(registry.convert("true", Boolean.class)).hasValue(true);
        assertThat(registry.convert("TRUE", Boolean.class)).hasValue(true);
        assertThat(registry.convert("yes", Boolean.class)).hasValue(true);
        assertThat(registry.convert("YES", Boolean.class)).hasValue(true);
        assertThat(registry.convert("1", Boolean.class)).hasValue(true);
        assertThat(registry.convert("  true  ", Boolean.class)).hasValue(true);
    }

    @Test
    public void convert_Boolean_FalseValues_ReturnsFalse() {
        assertThat(registry.convert("false", Boolean.class)).hasValue(false);
        assertThat(registry.convert("FALSE", Boolean.class)).hasValue(false);
        assertThat(registry.convert("no", Boolean.class)).hasValue(false);
        assertThat(registry.convert("NO", Boolean.class)).hasValue(false);
        assertThat(registry.convert("0", Boolean.class)).hasValue(false);
    }

    @Test
    public void convert_Boolean_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("maybe", Boolean.class)).isEmpty();
        assertThat(registry.convert("2", Boolean.class)).isEmpty();
    }

    // BigDecimal converter tests
    @Test
    public void convert_BigDecimal_ValidInput_ReturnsValue() {
        assertThat(registry.convert("123.456789", BigDecimal.class))
                .hasValue(new BigDecimal("123.456789"));
        assertThat(registry.convert("999999999999999999.99", BigDecimal.class))
                .hasValue(new BigDecimal("999999999999999999.99"));
    }

    @Test
    public void convert_BigDecimal_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("not a number", BigDecimal.class)).isEmpty();
    }

    // BigInteger converter tests
    @Test
    public void convert_BigInteger_ValidInput_ReturnsValue() {
        assertThat(registry.convert("12345678901234567890", BigInteger.class))
                .hasValue(new BigInteger("12345678901234567890"));
    }

    @Test
    public void convert_BigInteger_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("12.34", BigInteger.class)).isEmpty();
    }

    // URL converter tests
    @Test
    public void convert_URL_ValidInput_ReturnsValue() throws Exception {
        Optional<URL> result = registry.convert("https://example.com", URL.class);
        assertThat(result).isPresent();
        assertThat(result.get().toString()).isEqualTo("https://example.com");
    }

    @Test
    public void convert_URL_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("not a url", URL.class)).isEmpty();
    }

    // URI converter tests
    @Test
    public void convert_URI_ValidInput_ReturnsValue() {
        Optional<URI> result = registry.convert("https://example.com/path", URI.class);
        assertThat(result).isPresent();
        assertThat(result.get().toString()).isEqualTo("https://example.com/path");
    }

    @Test
    public void convert_URI_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("ht tp://invalid", URI.class)).isEmpty();
    }

    // Path converter tests
    @Test
    public void convert_Path_ValidInput_ReturnsValue() {
        Optional<Path> result = registry.convert("src/test/resources", Path.class);
        assertThat(result).hasValue(Paths.get("src/test/resources"));
    }

    // Duration converter tests
    @Test
    public void convert_Duration_ValidInput_ReturnsValue() {
        assertThat(registry.convert("PT30S", Duration.class))
                .hasValue(Duration.ofSeconds(30));
        assertThat(registry.convert("PT5M", Duration.class))
                .hasValue(Duration.ofMinutes(5));
        assertThat(registry.convert("PT2H", Duration.class))
                .hasValue(Duration.ofHours(2));
        assertThat(registry.convert("P1D", Duration.class))
                .hasValue(Duration.ofDays(1));
    }

    @Test
    public void convert_Duration_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("30 seconds", Duration.class)).isEmpty();
        assertThat(registry.convert("invalid", Duration.class)).isEmpty();
    }

    // Instant converter tests
    @Test
    public void convert_Instant_ValidInput_ReturnsValue() {
        Optional<Instant> result = registry.convert("2024-12-25T10:15:30Z", Instant.class);
        assertThat(result).isPresent();
        assertThat(result.get().toString()).isEqualTo("2024-12-25T10:15:30Z");
    }

    @Test
    public void convert_Instant_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("not a timestamp", Instant.class)).isEmpty();
    }

    // LocalDate converter tests
    @Test
    public void convert_LocalDate_ValidInput_ReturnsValue() {
        assertThat(registry.convert("2024-12-25", LocalDate.class))
                .hasValue(LocalDate.of(2024, 12, 25));
    }

    @Test
    public void convert_LocalDate_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("25/12/2024", LocalDate.class)).isEmpty();
        assertThat(registry.convert("invalid", LocalDate.class)).isEmpty();
    }

    // LocalDateTime converter tests
    @Test
    public void convert_LocalDateTime_ValidInput_ReturnsValue() {
        assertThat(registry.convert("2024-12-25T15:30:00", LocalDateTime.class))
                .hasValue(LocalDateTime.of(2024, 12, 25, 15, 30, 0));
    }

    @Test
    public void convert_LocalDateTime_InvalidInput_ReturnsEmpty() {
        assertThat(registry.convert("invalid datetime", LocalDateTime.class)).isEmpty();
    }

    // Null and edge case tests
    @Test
    public void convert_NullValue_ReturnsEmpty() {
        assertThat(registry.convert(null, String.class)).isEmpty();
        assertThat(registry.convert(null, Integer.class)).isEmpty();
    }

    @Test
    public void convert_UnregisteredType_ReturnsEmpty() {
        class CustomType {
        }
        assertThat(registry.convert("value", CustomType.class)).isEmpty();
    }

    // Registry management tests
    @Test
    public void hasConverter_RegisteredType_ReturnsTrue() {
        assertThat(registry.hasConverter(Integer.class)).isTrue();
        assertThat(registry.hasConverter(String.class)).isTrue();
        assertThat(registry.hasConverter(Duration.class)).isTrue();
    }

    @Test
    public void hasConverter_UnregisteredType_ReturnsFalse() {
        class CustomType {
        }
        assertThat(registry.hasConverter(CustomType.class)).isFalse();
    }

    @Test
    public void register_CustomConverter_Works() {
        class CustomType {
            final String value;

            CustomType(String value) {
                this.value = value;
            }
        }

        registry.register(CustomType.class, value -> Optional.of(new CustomType(value)));

        Optional<CustomType> result = registry.convert("test", CustomType.class);
        assertThat(result).isPresent();
        assertThat(result.get().value).isEqualTo("test");
    }

    @Test
    public void getInstance_ReturnsSameInstance() {
        TypeConverterRegistry instance1 = TypeConverterRegistry.getInstance();
        TypeConverterRegistry instance2 = TypeConverterRegistry.getInstance();
        assertThat(instance1).isSameAs(instance2);
    }
}
