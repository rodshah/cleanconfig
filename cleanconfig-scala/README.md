# CleanConfig Scala

Idiomatic Scala wrapper for CleanConfig with type-safe DSL and functional programming support.

## Features

- **Scala-Friendly DSL**: Named parameters instead of Java builder pattern
- **Type Safety**: Full Scala type system integration
- **Operator Overloading**: Use `&&` and `||` for validation rule composition
- **Scala Collections**: Native Scala collections (List, Map) instead of Java collections
- **Option Types**: Scala `Option` instead of Java `Optional`
- **Implicit Conversions**: Seamless Java/Scala interoperability
- **ScalaTest Integration**: Comprehensive test coverage with ScalaTest

## Installation

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

## Quick Start

```scala
import com.cleanconfig.scala._
import com.cleanconfig.scala.RuleOps._

implicit val integerClass: Class[Integer] = classOf[Integer]
implicit val stringClass: Class[String] = classOf[String]

// Define properties with named parameters
val serverPort = Property[Integer](
  name = "server.port",
  defaultValue = Some(8080),
  validationRule = Some(Rules.port)
)

val appName = Property[String](
  name = "app.name",
  validationRule = Some(Rules.notBlank && Rules.minLength(3))
)

// Build registry
val registry = PropertyRegistry()
  .registerAll(serverPort, appName)
  .build()

// Validate
val validator = PropertyValidator(registry)
val result = validator.validate(Map(
  "server.port" -> "8080",
  "app.name" -> "My Application"
))

if (result.isValid) {
  println("✓ Configuration valid")
} else {
  result.errors.foreach(e => println(s"✗ ${e.message}"))
}
```

## Operator Overloading

Compose validation rules with familiar operators:

```scala
import RuleOps._

// AND composition
val rule1 = Rules.notBlank && Rules.minLength(3) && Rules.maxLength(50)

// OR composition
val rule2 = Rules.startsWith("http://") || Rules.startsWith("https://")

// Complex composition
val emailRule = Rules.notBlank && Rules.email && Rules.endsWith("@company.com")
```

## Scala Collections

Work with native Scala collections:

```scala
// Scala List instead of java.util.List
val errors: List[ValidationError] = result.errors

// Scala Map instead of java.util.Map
val config = Map("key" -> "value")

// Scala Option instead of java.util.Optional
val value: Option[String] = context.getProperty("key")
```

## Architecture

```
┌─────────────────────────────────────────┐
│        Your Scala Application           │
│                                          │
│  Uses idiomatic Scala DSL:              │
│  - Named parameters                     │
│  - Operator overloading (&&, ||)        │
│  - Scala collections (List, Map)        │
│  - Option instead of Optional           │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│      CleanConfig Scala Wrappers         │
│                                          │
│  - Property (Scala DSL)                 │
│  - Rules (operator overloading)         │
│  - PropertyRegistry.Builder             │
│  - PropertyValidator                    │
│  - Implicit conversions                 │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│        CleanConfig Core (Java)          │
│                                          │
│  - PropertyDefinition                   │
│  - ValidationRule                       │
│  - PropertyRegistry                     │
│  - Type converters                      │
└─────────────────────────────────────────┘
```

## Available Validation Rules

### String Rules
`notBlank`, `minLength(n)`, `maxLength(n)`, `pattern(regex)`, `email`, `url`, `startsWith(prefix)`, `endsWith(suffix)`, `contains(substring)`

### Numeric Rules
`positive`, `negative`, `nonNegative`, `integerBetween(min, max)`, `longBetween(min, max)`, `doubleBetween(min, max)`, `port`

### File System Rules
`fileExists`, `readable`, `writable`, `directory`, `regularFile`

### URL/URI Rules
`urlWithProtocol(protocol)`, `uriWithScheme(scheme)`

### Time/Duration Rules
`positiveDuration`, `durationAtLeast(min)`, `durationAtMost(max)`, `inPast`, `inFuture`, `dateBefore(date)`, `dateAfter(date)`

### Custom Rules
```scala
val evenRule = Rules.custom[Int]("Must be even")(n => n % 2 == 0)
```

## Examples

See [ScalaDslExample.scala](src/main/scala/com/cleanconfig/scala/examples/ScalaDslExample.scala) for a comprehensive example demonstrating all features.

Run the example:
```bash
./gradlew :cleanconfig-scala:runExample
```

## Testing

```bash
# Run tests
./gradlew :cleanconfig-scala:test

# Run with coverage
./gradlew :cleanconfig-scala:test jacocoTestReport
```

## Compatibility

- **Scala Version**: 2.13.x
- **Java Version**: 11+
- **CleanConfig Core**: 0.1.0-SNAPSHOT

## Documentation

- [Scala Integration Guide](../docs/scala-integration.md) - Framework integrations, best practices, advanced patterns
- [Validation Rules](../docs/validation-rules.md) - Complete validation rules reference
- [Core Module](../cleanconfig-core/README.md) - Core CleanConfig concepts
- [Developer Guide](../docs/developer-guide.md) - Build, test, and contribute

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

Apache License 2.0 - see [LICENSE](../LICENSE) file for details.
