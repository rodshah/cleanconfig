# PropKit

**Type-safe property management library for Java 11+**

[![Build Status](https://img.shields.io/badge/build-pending-yellow)](https://github.com/rodshah/propkit)
[![Maven Central](https://img.shields.io/badge/maven--central-0.1.0--SNAPSHOT-blue)](https://central.sonatype.com/artifact/com.propkit/propkit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/java-11%2B-blue)](https://openjdk.org/)

---

## 🚧 Status: Under Active Development

This library is currently in early development. APIs may change. Star this repo to follow progress!

**Current Phase:** P1 - Production-Ready Features (Week 5/16)
**Progress:** 11/21 milestones complete (52%)

---

## Features

PropKit provides a powerful, type-safe approach to property management with:

- ✅ **Type-Safe Property Definitions** - No more `Map<String, String>`, use strongly-typed properties
- ✅ **Composable Validation Rules** - Build complex validation from simple, reusable rules
- ✅ **Conditional Default Values** - Defaults that depend on other property values
- ✅ **Dependency-Aware Validation** - Validates properties in the correct order
- ✅ **Multi-Property Validation** - Cross-property validation and property groups
- ✅ **Serialization Support** - Export/import properties in JSON, YAML, and Properties formats
- ✅ **Zero Dependencies** - Core module has no external dependencies
- ✅ **Java 11+ Compatible** - Works with Java 11 and all newer versions
- ✅ **Pluggable Logging** - Works with SLF4J, JUL, or custom loggers

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

See [Core Module README](cleanconfig-core/README.md) and [Serialization Module README](cleanconfig-serialization/README.md) for complete examples.

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

---

## Documentation

### Module Documentation
- [Core Module](cleanconfig-core/README.md) - Type-safe properties, validation, and defaults
- [Serialization Module](cleanconfig-serialization/README.md) - JSON/YAML/Properties serialization

### Feature Guides
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
```

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
