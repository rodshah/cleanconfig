# Developer Guide

Quick technical guide for CleanConfig developers.

## Quick Setup

```bash
git clone https://github.com/rodshah/cleanconfig.git
cd cleanconfig
./gradlew build
```

**Requirements:** JDK 11+, Gradle (wrapper included)

## Architecture

### Core Components

```
PropertyDefinition  → defines property metadata
PropertyRegistry    → stores definitions, validates dependencies
PropertyValidator   → validates values in dependency order
DefaultValueApplier → applies defaults without overriding users
TypeConverter       → converts strings to typed values
ValidationRule      → validates property values
```

### Design Patterns

**Builder Pattern**: `PropertyDefinition.builder()`, `PropertyRegistry.builder()`

**Functional Interfaces**: `ValidationRule`, `TypeConverter`, `ConditionalDefaultValue`

**Monadic Composition**: `Optional.flatMap()`, stream operations

**Strategy Pattern**: `ValidationRule`, `ConditionalDefaultValue`

**Topological Sort**: Dependency-aware validation order (Kahn's algorithm)

## Key Concepts

### Immutability

All core data structures are immutable after construction:

```java
// Registry is immutable after build()
PropertyRegistry registry = PropertyRegistry.builder()
    .register(property)
    .build();  // locks the registry
```

### Dependency Ordering

Properties with `dependsOnForValidation()` are validated after their dependencies:

```java
PropertyDefinition<String> cpuLimit = PropertyDefinition.builder(String.class)
    .name("cpu.limit")
    .dependsOnForValidation("cpu.request")  // validated AFTER cpu.request
    .build();
```

Registry builder detects circular dependencies at build time.

### Functional Composition

Validation rules compose using `and()`, `or()`, `onlyIf()`:

```java
ValidationRule<String> rule = Rules.notBlank()
    .and(Rules.email())
    .and(Rules.endsWith("@company.com"))
    .onlyIf(Conditions.propertyEquals("email.enabled", "true"));
```

### Type Safety

Values are strongly typed throughout:

```java
PropertyContext context = ...;

// Type-safe access
Optional<Integer> port = context.getTypedProperty("server.port", Integer.class);
Optional<Boolean> enabled = context.getTypedProperty("ssl.enabled", Boolean.class);
```

## Extension Points

### Custom Validation Rules

```java
public class CustomRules {
    public static ValidationRule<String> k8sResourceName() {
        return (name, value, context) -> {
            if (value.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?")) {
                return ValidationResult.success();
            }
            return ValidationResult.failure(
                ValidationError.builder()
                    .propertyName(name)
                    .actualValue(value)
                    .errorMessage("Invalid Kubernetes resource name")
                    .build()
            );
        };
    }
}
```

### Custom Type Converters

```java
TypeConverter<MyType> converter = value -> {
    try {
        return Optional.of(MyType.parse(value));
    } catch (Exception e) {
        return Optional.empty();
    }
};

TypeConverterRegistry.getInstance().register(MyType.class, converter);
```

### Custom Conditions

```java
Predicate<PropertyContext> customCondition = context ->
    context.getProperty("region").map(r -> r.startsWith("us-")).orElse(false);

ValidationRule<String> rule = Rules.notBlank().onlyIf(customCondition);
```

## Module Structure

```
cleanconfig-core/
├── PropertyDefinition     - Property metadata
├── PropertyRegistry       - Registry with dependency detection
├── PropertyValidator      - Validation engine
├── validation/
│   ├── ValidationRule     - Rule interface
│   ├── Rules              - Built-in rules facade
│   ├── Conditions         - Conditional execution
│   └── rules/             - Rule implementations by category
├── converter/
│   ├── TypeConverter      - Converter interface
│   └── TypeConverterRegistry - Converter registry
├── logging/               - Zero-dependency logging abstraction
└── impl/                  - Implementations
```

## Testing

Run tests:
```bash
./gradlew test
```

Coverage:
```bash
./gradlew jacocoTestReport
open cleanconfig-core/build/reports/jacoco/test/html/index.html
```

Target: >95% line coverage

### Test Patterns

```java
@Test
public void methodName_condition_expectedResult() {
    // Arrange
    PropertyDefinition<Integer> property = PropertyDefinition.builder(Integer.class)
            .name("test.prop")
            .validationRule(Rules.positive())
            .build();

    // Act
    ValidationResult result = validator.validate(properties);

    // Assert
    assertThat(result.isValid()).isTrue();
}
```

## Logging

Use CleanConfig logging abstraction:

```java
import com.cleanconfig.core.logging.Logger;
import com.cleanconfig.core.logging.LoggerFactory;

Logger log = LoggerFactory.getLogger(MyClass.class);

log.debug("Validating properties: {}", propertyNames);
log.info("Validation completed with {} errors", errorCount);
log.error("Failed to validate property: {}", name, exception);
```

Auto-detects SLF4J, falls back to JUL, or uses No-Op provider.

## Code Quality

Run all checks:
```bash
./gradlew check
```

Individual checks:
```bash
./gradlew checkstyleMain checkstyleTest   # Code style
./gradlew spotbugsMain spotbugsTest       # Static analysis
./gradlew javadoc                         # Documentation
```

## Performance Considerations

- Validation order computed once at registry build time
- Type converters cached in singleton registry
- Immutable collections prevent defensive copying
- Stream operations for functional clarity (profiled)
- No reflection in validation hot paths

## Common Tasks

### Add New Validation Rule

1. Create rule in appropriate `*Rules` class
2. Add delegation method in `Rules` facade
3. Write tests in corresponding test class
4. Update documentation

### Add New Type Converter

1. Create converter implementation
2. Register in `TypeConverterRegistry` static initializer
3. Write tests
4. Update type-conversion.md

### Add New Module

1. Create module directory
2. Add to `settings.gradle`
3. Create `build.gradle` for module
4. Create module README.md

## See Also

- [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution workflow
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed design (Coming Soon)
- [Core Module README](../cleanconfig-core/README.md) - API documentation

## License

Apache License 2.0
