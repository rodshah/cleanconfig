# CleanConfig Serialization

Optional serialization support for CleanConfig properties in multiple formats.

## Overview

Serialize and deserialize configuration properties in:
- **Properties**: Standard Java Properties files
- **JSON**: Structured JSON with Jackson
- **YAML**: Human-readable YAML with Jackson YAML

Features: Round-trip serialization, optional metadata, default values, multiple output targets.

## Installation

### Gradle

```groovy
implementation 'com.cleanconfig:cleanconfig-serialization:0.1.0-SNAPSHOT'

// Optional: For JSON support
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'

// Optional: For YAML support
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2'
```

## Quick Start

### Basic Serialization

```java
import com.cleanconfig.serialization.*;

// Properties format (no dependencies)
PropertiesSerializer propertiesSerializer = new PropertiesSerializer();

// JSON format (requires Jackson)
JsonSerializer jsonSerializer = new JsonSerializer();

// YAML format (requires Jackson YAML)
YamlSerializer yamlSerializer = new YamlSerializer();

// Serialize
String output = serializer.serialize(
    properties,           // Map<String, String>
    registry,             // PropertyRegistry
    SerializationOptions.defaults()
);

// Deserialize
Map<String, String> loaded = serializer.deserialize(output);
```

### Serialization Options

```java
SerializationOptions options = SerializationOptions.builder()
    .prettyPrint(true)           // Pretty output (JSON)
    .includeMetadata(true)        // Include type, category
    .includeDescriptions(true)    // Include descriptions
    .includeDefaults(true)        // Include default values
    .build();

// Presets
SerializationOptions.defaults();  // Basic
SerializationOptions.compact();   // Minimal
SerializationOptions.verbose();   // All metadata
```

### Output Examples

**Properties Format:**
```properties
# Database host
# Type: String
db.host=localhost
# Database port
# Type: Integer
db.port=5432
```

**JSON Format:**
```json
{
  "properties": {
    "db.host": "localhost",
    "db.port": "5432"
  },
  "metadata": {
    "db.host": {
      "type": "String",
      "description": "Database host"
    }
  }
}
```

**YAML Format:**
```yaml
properties:
  db.host: localhost
  db.port: '5432'
metadata:
  db.host:
    type: String
    description: Database host
```

### Multiple Output Targets

```java
// String
String output = serializer.serialize(properties, registry, options);

// OutputStream
try (FileOutputStream fos = new FileOutputStream("config.yaml")) {
    serializer.serialize(properties, registry, options, fos);
}

// Writer
try (FileWriter writer = new FileWriter("config.yaml")) {
    serializer.serialize(properties, registry, options, writer);
}
```

## Complete Example

```java
// Define properties
PropertyDefinition<String> dbHost = PropertyDefinition.builder(String.class)
    .name("db.host")
    .description("Database host")
    .defaultValue("localhost")
    .category(PropertyCategory.DATABASE)
    .build();

// Create registry
PropertyRegistry registry = PropertyRegistry.builder()
    .register(dbHost)
    .build();

// User properties
Map<String, String> properties = Map.of("db.host", "prod-db.example.com");

// Serialize to YAML
YamlSerializer serializer = new YamlSerializer();
String yaml = serializer.serialize(
    properties,
    registry,
    SerializationOptions.verbose()
);

// Load back
Map<String, String> loaded = serializer.deserialize(yaml);
```

## Format Comparison

| Format | Dependencies | Metadata | Human-Editable |
|--------|-------------|----------|----------------|
| Properties | None | Comments | ✓ |
| JSON | Jackson | Structured | ✓ |
| YAML | Jackson YAML | Structured | ✓✓ |

## Thread Safety

All serializers are thread-safe and stateless.

## See Also

- [Core Module](../cleanconfig-core/README.md)
- [Main README](../README.md)

## License

Apache License 2.0
