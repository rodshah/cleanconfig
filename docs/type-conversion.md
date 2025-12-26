# Type Conversion in PropKit

PropKit provides automatic type conversion from string property values to typed values.

## Built-in Converters

The `TypeConverterRegistry` includes 17 built-in converters:

### Primitives & Wrappers
- **String** - Identity conversion
- **Integer** - Parses integers (e.g., "42", "-100")
- **Long** - Parses long values
- **Double** - Parses doubles (e.g., "3.14", "1.23e10")
- **Float** - Parses floats
- **Short** - Parses short values
- **Byte** - Parses byte values
- **Boolean** - Accepts: true/false, yes/no, 1/0 (case-insensitive)

### Big Numbers
- **BigDecimal** - Arbitrary precision decimals
- **BigInteger** - Arbitrary precision integers

### URLs & Files
- **URL** - Parses URLs (e.g., "https://example.com")
- **URI** - Parses URIs
- **Path** - Converts to NIO Path

### Date & Time (Java 8+)
- **Duration** - ISO-8601 format (e.g., "PT30S", "PT5M", "PT2H")
- **Instant** - ISO-8601 timestamps (e.g., "2024-12-25T10:15:30Z")
- **LocalDate** - ISO-8601 dates (e.g., "2024-12-25")
- **LocalDateTime** - ISO-8601 date-times (e.g., "2024-12-25T15:30:00")

## Usage

### Direct Conversion

```java
TypeConverterRegistry registry = TypeConverterRegistry.getInstance();

Optional<Integer> port = registry.convert("8080", Integer.class);
Optional<Duration> timeout = registry.convert("PT30S", Duration.class);
Optional<Boolean> enabled = registry.convert("true", Boolean.class);
```

### With PropertyContext

```java
PropertyContext context = ...;

// Typed property access uses converters automatically
Optional<Integer> threads = context.getTypedProperty("thread.count", Integer.class);
Optional<Duration> timeout = context.getTypedProperty("timeout", Duration.class);
```

## Custom Converters

Register custom converters for your types:

```java
TypeConverterRegistry registry = TypeConverterRegistry.getInstance();

// Register converter
registry.register(MyType.class, value -> {
    try {
        return Optional.of(MyType.parse(value));
    } catch (Exception e) {
        return Optional.empty();
    }
});

// Use converter
Optional<MyType> result = registry.convert("value", MyType.class);
```

## Conversion Examples

### Boolean Values
```java
convert("true", Boolean.class)   // → true
convert("TRUE", Boolean.class)   // → true
convert("yes", Boolean.class)    // → true
convert("1", Boolean.class)      // → true
convert("false", Boolean.class)  // → false
convert("no", Boolean.class)     // → false
convert("0", Boolean.class)      // → false
convert("maybe", Boolean.class)  // → empty
```

### Duration Values
```java
convert("PT30S", Duration.class)  // → 30 seconds
convert("PT5M", Duration.class)   // → 5 minutes
convert("PT2H", Duration.class)   // → 2 hours
convert("P1D", Duration.class)    // → 1 day
```

### Date/Time Values
```java
convert("2024-12-25", LocalDate.class)
// → LocalDate.of(2024, 12, 25)

convert("2024-12-25T15:30:00", LocalDateTime.class)
// → LocalDateTime.of(2024, 12, 25, 15, 30, 0)

convert("2024-12-25T10:15:30Z", Instant.class)
// → Instant representing 2024-12-25 10:15:30 UTC
```

## Error Handling

All converters return `Optional<T>`:
- **Present**: Conversion succeeded
- **Empty**: Conversion failed (invalid format, out of range, etc.)

```java
Optional<Integer> result = registry.convert("abc", Integer.class);
if (result.isPresent()) {
    int value = result.get();
} else {
    // Handle conversion failure
}
```

## Thread Safety

`TypeConverterRegistry` is thread-safe:
- Singleton instance
- Concurrent converter registration
- Safe for use across multiple threads

## Whitespace Handling

Most converters automatically trim whitespace:

```java
convert("  42  ", Integer.class)  // → 42
convert(" true ", Boolean.class)  // → true
```
