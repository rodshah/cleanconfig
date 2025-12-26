# CleanConfig Core

Type-safe property management library for Java 11+.

## Overview

CleanConfig Core provides the foundational APIs for type-safe property definition, validation, and management. It includes:

- **Property Definitions**: Type-safe metadata about configuration properties
- **Validation System**: Composable validation rules with dependency awareness
- **Default Values**: Static, conditional, and computed defaults
- **Type Conversion**: Built-in converters for 17+ Java types
- **Logging Abstraction**: Zero-dependency logging with SLF4J/JUL support
- **Property Registry**: Thread-safe registry with circular dependency detection
- **Property Validator**: Topological-sort-based validation engine
- **Default Value Applier**: Pure function for applying defaults

## Installation

### Maven

```xml
<dependency>
    <groupId>com.cleanconfig</groupId>
    <artifactId>cleanconfig-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.cleanconfig:cleanconfig-core:0.1.0-SNAPSHOT'
```

## Quick Examples

### Basic Property Definition

```java
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.validation.Rules;

PropertyDefinition<Integer> serverPort = PropertyDefinition.builder(Integer.class)
    .name("server.port")
    .description("HTTP server port")
    .defaultValue(8080)
    .validationRule(Rules.port())
    .required(true)
    .build();
```

### Property Registry

```java
import com.cleanconfig.core.PropertyRegistry;

PropertyRegistry registry = PropertyRegistry.builder()
    .register(serverPort)
    .register(serverHost)
    .register(maxConnections)
    .build();
```

### Validation

```java
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.ValidationResult;

PropertyValidator validator = new DefaultPropertyValidator(registry);

Map<String, String> userProperties = Map.of(
    "server.port", "8080",
    "server.host", "localhost"
);

ValidationResult result = validator.validate(userProperties);

if (result.isValid()) {
    System.out.println("✓ All properties valid!");
} else {
    result.getErrors().forEach(error ->
        System.err.println("✗ " + error.getPropertyName() + ": " + error.getErrorMessage())
    );
}
```

### Composable Rules

```java
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationRule;

ValidationRule<String> emailRule = Rules.notBlank()
    .and(Rules.email())
    .and(Rules.endsWith("@company.com"));
```

### Conditional Defaults

```java
import com.cleanconfig.core.ConditionalDefaultValue;

PropertyDefinition<Integer> retryCount = PropertyDefinition.builder(Integer.class)
    .name("retry.count")
    .defaultValue(ConditionalDefaultValue.computed(ctx ->
        ctx.getTypedProperty("retry.enabled", Boolean.class).orElse(false)
            ? Optional.of(3)
            : Optional.of(0)
    ))
    .build();
```

### Dependency-Aware Validation

```java
PropertyDefinition<String> cpuLimit = PropertyDefinition.builder(String.class)
    .name("cpu.limit")
    .dependsOnForValidation("cpu.request")
    .validationRule(Rules.notBlank().and((name, value, ctx) -> {
        String request = ctx.getProperty("cpu.request").orElse("");
        // Validate that limit >= request
        return ValidationResult.success();
    }))
    .build();
```

### Applying Defaults

```java
import com.cleanconfig.core.DefaultValueApplier;
import com.cleanconfig.core.DefaultApplicationResult;
import com.cleanconfig.core.impl.DefaultValueApplierImpl;

DefaultValueApplier applier = new DefaultValueApplierImpl(registry);

Map<String, String> userProps = Map.of("server.host", "localhost");

DefaultApplicationResult result = applier.applyDefaults(userProps);

// Get final properties (user values + defaults)
Map<String, String> finalProps = result.getPropertiesWithDefaults();

// See which defaults were applied
result.getApplicationInfo().getAppliedDefaults()
    .forEach((name, value) ->
        System.out.println("Applied default: " + name + " = " + value)
    );
```

## Core APIs

### PropertyDefinition

Metadata about a configuration property including type, validation rules, defaults, and dependencies.

**Key Methods:**
- `getName()` - Property name
- `getType()` - Value type class
- `getValidationRule()` - Validation rule
- `getDefaultValue()` - Default value provider
- `isRequired()` - Required/optional flag
- `getDependsOnForValidation()` - Validation dependencies

### PropertyRegistry

Thread-safe registry for property definitions with circular dependency detection.

**Key Methods:**
- `getProperty(String name)` - Get property by name
- `isDefined(String name)` - Check if property exists
- `getAllProperties()` - Get all registered properties
- `getAllPropertyNames()` - Get all property names

### PropertyValidator

Validates properties in dependency-aware order using topological sort.

**Key Methods:**
- `validate(Map<String, String> properties)` - Validate all properties
- `validate(Map<String, String> properties, ValidationContextType contextType)` - Context-aware validation
- `validateProperty(String name, String value, Map<String, String> properties)` - Validate single property

### ValidationRule&lt;T&gt;

Functional interface for property validation with composition support.

**Key Methods:**
- `validate(String name, T value, PropertyContext context)` - Validate value
- `and(ValidationRule<T> other)` - Logical AND composition
- `or(ValidationRule<T> other)` - Logical OR composition
- `onlyIf(Predicate<PropertyContext> condition)` - Conditional execution

### ConditionalDefaultValue&lt;T&gt;

Provider for default values with support for static, conditional, and computed defaults.

**Factory Methods:**
- `staticValue(T value)` - Static default
- `computed(Function<PropertyContext, Optional<T>> computer)` - Computed default
- `when(Predicate<PropertyContext> condition)` - Conditional default

### DefaultValueApplier

Applies default values to properties without overriding user-provided values.

**Key Methods:**
- `applyDefaults(Map<String, String> userProperties)` - Apply defaults
- `applyDefaults(Map<String, String> userProperties, ValidationContextType contextType)` - Context-aware application

## Validation Rules

CleanConfig provides 40+ built-in validation rules via the `Rules` factory class:

### String Rules
- `notBlank()`, `notEmpty()`, `minLength(int)`, `maxLength(int)`
- `matchesRegex(String)`, `matchesPattern(Pattern)`
- `email()`, `url()`, `startsWith(String)`, `endsWith(String)`
- `containsOnly(String)`, `alphanumeric()`, `alpha()`, `numeric()`

### Numeric Rules
- `positive()`, `negative()`, `nonNegative()`, `nonPositive()`
- `integerBetween(int, int)`, `longBetween(long, long)`, `doubleBetween(double, double)`
- `port()`, `greaterThan(int)`, `lessThan(int)`

### File Rules
- `fileExists()`, `directoryExists()`, `readable()`, `writable()`, `executable()`
- `directory()`, `regularFile()`, `hasExtension(String)`

### General Rules
- `required()`, `oneOf(Collection)`, `custom(BiPredicate)`

### Composite Rules
- `allOf(ValidationRule<T>...)` - All rules must pass
- `anyOf(ValidationRule<T>...)` - At least one rule must pass

## Type Conversion

Built-in converters for 17 Java types:

- **Primitives**: Integer, Long, Double, Float, Short, Byte, Boolean
- **Big Numbers**: BigDecimal, BigInteger
- **Strings**: String
- **Networking**: URL, URI
- **File System**: Path
- **Date/Time**: Duration, Instant, LocalDate, LocalDateTime

### Custom Type Converters

```java
import com.cleanconfig.core.converter.TypeConverter;
import com.cleanconfig.core.converter.TypeConverterRegistry;

TypeConverter<MyType> converter = value -> {
    // Convert string to MyType
    return Optional.of(new MyType(value));
};

TypeConverterRegistry.getInstance().register(MyType.class, converter);
```

## Conditional Validation

Use `Conditions` factory to create conditional validation:

```java
import com.cleanconfig.core.validation.Conditions;

ValidationRule<String> sslCertRule = Rules.fileExists()
    .onlyIf(Conditions.propertyEquals("ssl.enabled", "true"));

ValidationRule<String> passwordRule = Rules.required()
    .onlyIf(Conditions.contextTypeIs(ValidationContextType.PRODUCTION));
```

### Available Conditions
- `propertyEquals(String, String)` - Property equals value
- `propertyNotEquals(String, String)` - Property not equals value
- `propertyIsPresent(String)` - Property has any value
- `propertyIsAbsent(String)` - Property not set
- `propertyIsTrue(String)` - Property is "true"
- `propertyIsFalse(String)` - Property is "false"
- `contextTypeIs(ValidationContextType)` - Context type matches
- `metadataEquals(String, String)` - Metadata equals value
- `and()`, `or()`, `not()` - Logical composition

## Logging

CleanConfig uses zero-dependency logging abstraction:

```java
import com.cleanconfig.core.logging.LoggerFactory;
import com.cleanconfig.core.logging.Logger;

Logger log = LoggerFactory.getLogger(MyClass.class);
log.info("Validating properties");
```

### Logging Providers
- **SLF4J** - Auto-detected if available
- **JUL** - Java Util Logging fallback
- **No-Op** - Silent mode

### Custom Logger

```java
import com.cleanconfig.core.logging.LoggerProvider;

LoggerFactory.setLoggerProvider(customProvider);
```

## Context-Based Configuration

Different validation rules for different contexts:

```java
import com.cleanconfig.core.ValidationContextType;

// Validate for startup
ValidationResult startup = validator.validate(props, ValidationContextType.STARTUP);

// Validate for runtime override
ValidationResult runtime = validator.validate(props, ValidationContextType.RUNTIME_OVERRIDE);
```

### Context Types
- `STARTUP` - Application startup
- `RUNTIME_OVERRIDE` - Runtime property changes
- `TESTING` - Test environment
- `PRODUCTION` - Production environment

## Thread Safety

All core APIs are thread-safe:
- `PropertyRegistry` is immutable after build
- `PropertyValidator` is stateless
- `DefaultValueApplier` is stateless
- `TypeConverterRegistry` uses concurrent collections

## Performance

CleanConfig is designed for performance:
- Validation uses topological sort for optimal order
- Registry uses LinkedHashMap for O(1) lookups
- Type converters are cached in singleton registry
- No reflection in hot paths

## Dependencies

CleanConfig Core has **zero runtime dependencies**:
- No forced logging framework (abstraction only)
- No JSON/YAML libraries
- No DI frameworks
- Pure Java 11+ standard library

## Migration from Properties

```java
// Before: Untyped, error-prone
Properties props = new Properties();
int port = Integer.parseInt(props.getProperty("server.port", "8080"));

// After: Type-safe, validated
PropertyDefinition<Integer> PORT = PropertyDefinition.builder(Integer.class)
    .name("server.port")
    .defaultValue(8080)
    .validationRule(Rules.port())
    .build();

PropertyRegistry registry = PropertyRegistry.builder()
    .register(PORT)
    .build();
```

## See Also

- [Main README](../README.md) - Project overview
- [Validation Rules Documentation](../docs/validation-rules.md)
- [Type Conversion Documentation](../docs/type-conversion.md)
- [Logging Documentation](../docs/logging.md)
- [Developer Guide](../docs/developer-guide.md)

## License

Apache License 2.0
