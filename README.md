# PropKit

**Type-safe property management library for Java 11+**

[![Build Status](https://img.shields.io/badge/build-pending-yellow)](https://github.com/rodshah/propkit)
[![Maven Central](https://img.shields.io/badge/maven--central-0.1.0--SNAPSHOT-blue)](https://central.sonatype.com/artifact/com.propkit/propkit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/java-11%2B-blue)](https://openjdk.org/)

---

## 🚧 Status: Under Active Development

This library is currently in early development. APIs may change. Star this repo to follow progress!

**Current Phase:** P0 - Core Library Foundation (Week 1/16)

---

## Features

PropKit provides a powerful, type-safe approach to property management with:

- ✅ **Type-Safe Property Definitions** - No more `Map<String, String>`, use strongly-typed properties
- ✅ **Composable Validation Rules** - Build complex validation from simple, reusable rules
- ✅ **Conditional Default Values** - Defaults that depend on other property values
- ✅ **Dependency-Aware Validation** - Validates properties in the correct order
- ✅ **Context-Based Configuration** - Different validation rules for different contexts
- ✅ **Zero Dependencies** - Core module has no external dependencies
- ✅ **Java 11+ Compatible** - Works with Java 11 and all newer versions
- ✅ **Pluggable Logging** - Works with SLF4J, JUL, or custom loggers

---

## Quick Start

Coming soon... (after MVP release)

```java
// Preview of what's coming:

// Define a type-safe property
PropertyDefinition<Integer> SERVER_PORT = PropertyDefinition.builder(Integer.class)
    .name("server.port")
    .defaultValue(8080)
    .validationRule(Rules.integerBetween(1024, 65535))
    .description("HTTP server port")
    .build();

// Create a registry
PropertyRegistry registry = PropertyRegistryBuilder.create()
    .register(SERVER_PORT)
    .build();

// Validate properties
PropertyValidator validator = new DefaultPropertyValidator(registry);
ValidationResult result = validator.validate(userProperties);

if (result.isValid()) {
    System.out.println("✓ All properties valid!");
} else {
    result.getErrors().forEach(error ->
        System.err.println("✗ " + error.getPropertyName() + ": " + error.getErrorMessage())
    );
}
```

---

## Installation

**Maven** (coming soon):
```xml
<dependency>
    <groupId>com.propkit</groupId>
    <artifactId>propkit-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle** (coming soon):
```groovy
implementation 'com.propkit:propkit-core:0.1.0'
```

---

## Documentation

- [Getting Started Guide](docs/getting-started.md) (Coming Soon)
- [Validation Rules](docs/validation-rules.md) ✅
- [Type Conversion](docs/type-conversion.md) ✅
- [Logging](docs/logging.md) ✅
- [API Reference](https://cleanconfig.dev/api) (Coming Soon)

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
