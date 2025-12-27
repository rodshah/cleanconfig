# Serialization Guide

Serialize and deserialize CleanConfig properties in Properties, JSON, or YAML format.

## Installation

```groovy
implementation 'com.propkit:cleanconfig-serialization:0.1.0-SNAPSHOT'

// Optional: For JSON/YAML support
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2'
```

## Quick Start

### Properties Format (No Dependencies)

```java
PropertiesSerializer serializer = new PropertiesSerializer();
String output = serializer.serialize(properties, registry, SerializationOptions.defaults());
Map<String, String> loaded = serializer.deserialize(output);
```

### JSON Format

```java
JsonSerializer serializer = new JsonSerializer();
String json = serializer.serialize(properties, registry,
    SerializationOptions.defaults().toBuilder().prettyPrint(true).build());
Map<String, String> loaded = serializer.deserialize(json);
```

### YAML Format

```java
YamlSerializer serializer = new YamlSerializer();
String yaml = serializer.serialize(properties, registry, SerializationOptions.defaults());
Map<String, String> loaded = serializer.deserialize(yaml);
```

## Serialization Options

```java
SerializationOptions options = SerializationOptions.builder()
    .prettyPrint(true)           // Pretty output (JSON only)
    .includeMetadata(true)        // Include type, category
    .includeDescriptions(true)    // Include descriptions
    .includeDefaults(true)        // Include default values
    .build();

// Presets
SerializationOptions.defaults();  // Basic
SerializationOptions.compact();   // Minimal
SerializationOptions.verbose();   // All metadata
```

## Output Examples

### YAML with Metadata

```yaml
properties:
  db.host: localhost
  db.port: '5432'
metadata:
  db.host:
    type: String
    category: DATABASE
    description: Database host
```

### Properties with Comments

```properties
# Database host
# Type: String
db.host=localhost
```

### JSON

```json
{
  "properties": {
    "db.host": "localhost"
  },
  "metadata": {
    "db.host": {
      "type": "String"
    }
  }
}
```

## Output Targets

```java
// String
String output = serializer.serialize(properties, registry, options);

// File (OutputStream)
try (FileOutputStream fos = new FileOutputStream("config.yaml")) {
    serializer.serialize(properties, registry, options, fos);
}

// Writer
try (FileWriter writer = new FileWriter("config.yaml")) {
    serializer.serialize(properties, registry, options, writer);
}
```

## Format Selection

| Format | Dependencies | Best For |
|--------|-------------|----------|
| Properties | None | Legacy systems, simple configs |
| JSON | Jackson | APIs, structured data |
| YAML | Jackson YAML | Configuration files, DevOps |

## See Also

- [Serialization Module README](../cleanconfig-serialization/README.md)
- [Core Module](../cleanconfig-core/README.md)
