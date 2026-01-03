# PropKit

**Type-safe property management library for Java 11+**

[![Build Status](https://img.shields.io/badge/build-pending-yellow)](https://github.com/rodshah/propkit)
[![Maven Central](https://img.shields.io/badge/maven--central-0.1.0--SNAPSHOT-blue)](https://central.sonatype.com/artifact/com.propkit/propkit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/java-11%2B-blue)](https://openjdk.org/)

---

## Features

PropKit provides a powerful, type-safe approach to property management with:

- ✅ **Type-Safe Property Definitions** - No more `Map<String, String>`, use strongly-typed properties
- ✅ **Composable Validation Rules** - Build complex validation from simple, reusable rules
- ✅ **Conditional Default Values** - Defaults that depend on other property values
- ✅ **Dependency-Aware Validation** - Validates properties in the correct order
- ✅ **Multi-Property Validation** - Cross-property validation and property groups
- ✅ **Enhanced Error Messages** - Clear errors with actual/expected values and suggestions
- ✅ **Multiple Output Formatters** - Text and JSON formatters for different use cases
- ✅ **Serialization Support** - Export/import properties in JSON, YAML, and Properties formats
- ✅ **Spring Boot Integration** - Auto-configuration starter for seamless Spring Boot integration
- ✅ **Scala Wrapper** - Idiomatic Scala DSL with operator overloading and functional programming support
- ✅ **Zero Dependencies** - Core module has no external dependencies
- ✅ **Java 11+ Compatible** - Works with Java 11 and all newer versions
- ✅ **Pluggable Logging** - Works with SLF4J, JUL, or custom loggers

---

## Performance

PropKit is designed for high-performance property validation with minimal overhead:

| Operation | Throughput | Notes |
|-----------|------------|-------|
| Validation (small config) | **1,767 ops/ms** | 10 properties with validation rules |
| Validation (medium config) | **498 ops/ms** | 50 properties with validation rules |
| Validation (large config) | **133 ops/ms** | 200 properties with validation rules |
| **Cached validation** | **7,118 ops/ms** | **14.5x faster** with cache enabled |
| Properties serialization | **565 ops/ms** | Fastest format, zero dependencies |
| JSON serialization | **397 ops/ms** | With metadata support |
| YAML serialization | **74 ops/ms** | Human-readable format |

**Optional Performance Features:**
- **Validation Caching**: 14.5x speedup for repeated validations
- **Computed Default Memoization**: Cache expensive calculations (system calls, I/O)
- **Thread-Safe**: All caching features use concurrent data structures

See [Performance Guide](docs/performance.md) for benchmarking details, optimization strategies, and usage examples.

*Benchmarks: JMH 1.37, 3 warmup iterations, 5 measurement iterations, 2 forks on MacBook Pro M4 Max (16 cores, 64GB RAM, OpenJDK 11).*

---

## Quick Start

### Basic Usage

```java
// Define type-safe properties
PropertyDefinition<Integer> serverPort = PropertyDefinition.builder(Integer.class)
    .name("server.port")
    .defaultValue(8080)
    .validationRule(Rules.port())
    .build();

// Create registry and validate
PropertyRegistry registry = PropertyRegistry.builder()
    .register(serverPort)
    .build();

PropertyValidator validator = new DefaultPropertyValidator(registry);
ValidationResult result = validator.validate(properties);
```

### With Serialization

```java
// Serialize properties to YAML
YamlSerializer serializer = new YamlSerializer();
String yaml = serializer.serialize(
    properties,
    registry,
    SerializationOptions.verbose()
);

// Deserialize back
Map<String, String> loaded = serializer.deserialize(yaml);
```

### With Spring Boot

```java
// 1. Add dependency
implementation 'com.propkit:cleanconfig-spring-boot-starter:0.1.0-SNAPSHOT'

// 2. Define property schemas as beans
@Configuration
public class AppConfig {
    @Bean
    public PropertyDefinition<String> appName() {
        return PropertyDefinition.builder(String.class)
                .name("app.name")
                .validationRule(Rules.notBlank().and(Rules.minLength(3)))
                .build();
    }
}

// 3. Provide values in application.properties
app.name=My Application

// 4. Use validated properties
@RestController
public class MyController {
    @Value("${app.name}")
    private String appName;
}
```

### With Scala

```scala
import com.cleanconfig.scala._
import com.cleanconfig.scala.RuleOps._

implicit val intClass: Class[Int] = classOf[Int]
implicit val stringClass: Class[String] = classOf[String]

// Define properties with Scala DSL
val serverPort = Property[Int](
  name = "server.port",
  defaultValue = Some(8080),
  validationRule = Some(Rules.port)
)

val appName = Property[String](
  name = "app.name",
  validationRule = Some(Rules.notBlank && Rules.minLength(3))
)

// Build registry and validate
val registry = PropertyRegistry()
  .registerAll(serverPort, appName)
  .build()

val validator = PropertyValidator(registry)
val result = validator.validate(Map("server.port" -> "8080", "app.name" -> "My App"))
```

See [Core Module README](cleanconfig-core/README.md), [Scala Module README](cleanconfig-scala/README.md), [Serialization Module README](cleanconfig-serialization/README.md), and [Spring Boot Integration](docs/spring-boot-integration.md) for complete examples.

---

## Installation

### Core Module

**Maven**:
```xml
<dependency>
    <groupId>com.propkit</groupId>
    <artifactId>propkit-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**:
```groovy
implementation 'com.propkit:propkit-core:0.1.0-SNAPSHOT'
```

### Serialization Module (Optional)

**Maven**:
```xml
<dependency>
    <groupId>com.propkit</groupId>
    <artifactId>cleanconfig-serialization</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- For JSON support -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<!-- For YAML support -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.15.2</version>
</dependency>
```

**Gradle**:
```groovy
implementation 'com.propkit:cleanconfig-serialization:0.1.0-SNAPSHOT'

// Optional: For JSON support
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'

// Optional: For YAML support
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2'
```

### Spring Boot Starter (Optional)

**Maven**:
```xml
<dependency>
    <groupId>com.propkit</groupId>
    <artifactId>cleanconfig-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**:
```groovy
implementation 'com.propkit:cleanconfig-spring-boot-starter:0.1.0-SNAPSHOT'
```

### Scala Module (Optional)

**Gradle**:
```groovy
implementation 'com.propkit:cleanconfig-scala:0.1.0-SNAPSHOT'
```

**SBT**:
```scala
libraryDependencies += "com.propkit" %% "cleanconfig-scala" % "0.1.0-SNAPSHOT"
```

**Maven**:
```xml
<dependency>
    <groupId>com.propkit</groupId>
    <artifactId>cleanconfig-scala_2.13</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

---

## Documentation

### Module Documentation
- [Core Module](cleanconfig-core/README.md) - Type-safe properties, validation, and defaults
- [Serialization Module](cleanconfig-serialization/README.md) - JSON/YAML/Properties serialization
- [Spring Boot Starter](cleanconfig-spring-boot-example/README.md) - Auto-configuration and examples
- [Scala Module](cleanconfig-scala/README.md) - Idiomatic Scala DSL and functional programming

### Feature Guides
- [Spring Boot Integration](docs/spring-boot-integration.md) - Architecture and usage guide
- [Scala Integration](docs/scala-integration.md) - Scala DSL and framework integration
- [Validation Rules](docs/validation-rules.md) - 40+ built-in validation rules
- [Advanced Validation](docs/advanced-validation.md) - Multi-property validation and groups
- [Type Conversion](docs/type-conversion.md) - Built-in converters and custom types
- [Logging](docs/logging.md) - Zero-dependency logging abstraction
- [Developer Guide](docs/developer-guide.md) - Build, test, and contribute

### API Reference
- [Javadoc](https://cleanconfig.dev/api) (Coming Soon)

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/rodshah/propkit.git
cd propkit

# Build (requires JDK 11+)
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# Run performance benchmarks (optional, ~10 minutes)
./gradlew :cleanconfig-benchmarks:jmh
# Results saved to: cleanconfig-benchmarks/build/results/jmh/results.txt
```

See [cleanconfig-benchmarks/README.md](cleanconfig-benchmarks/README.md) for benchmark details.

---

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## Inspiration

CleanConfig is inspired by:
- The Clean Code movement (Robert C. Martin)
- FluentValidation (C#)
- Viper (Go)
- TypeScript's type system

Built to solve real-world configuration management pain points in large-scale Java applications.

---

**Made with ❤️ for the Java community**

[⭐ Star this repo](https://github.com/rodshah/cleanconfig) | [📖 Documentation](https://cleanconfig.dev) | [🐛 Report Issues](https://github.com/rodshah/cleanconfig/issues)
