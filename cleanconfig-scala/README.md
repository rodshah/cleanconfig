# CleanConfig Scala

Idiomatic Scala wrapper for CleanConfig with type-safe DSL and functional programming support.

## Features

- **ConfigLoader**: Load validated config directly into Scala case classes
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
implementation 'com.cleanconfig:cleanconfig-scala:0.3.0-SNAPSHOT'
```

**SBT**:
```scala
libraryDependencies += "com.cleanconfig" %% "cleanconfig-scala" % "0.3.0-SNAPSHOT"
```

**Maven**:
```xml
<dependency>
    <groupId>com.cleanconfig</groupId>
    <artifactId>cleanconfig-scala_2.13</artifactId>
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

## ConfigLoader — Case Class Binding

Load a `Map[String, String]` directly into typed, validated Scala case classes. Replaces the manual define-register-validate-extract workflow.

```scala
import com.cleanconfig.scala._
import com.cleanconfig.scala.ConfigLoader._
import com.cleanconfig.scala.RuleOps._

// Define case classes
case class DbConfig(url: String, maxPool: Int)
case class AppConfig(name: String, port: Int, db: DbConfig, desc: Option[String])

// Define loaders with validation rules and defaults
implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
  field[String]("url", Rules.notBlank && Rules.startsWith("jdbc:")),
  field[Int]("max.pool", Rules.integerBetween(1, 100)).withDefault(10)
)(DbConfig.apply)

implicit val appLoader: ConfigLoader[AppConfig] = ConfigLoader.build(
  field[String]("app.name", Rules.notBlank),
  field[Int]("app.port", Rules.port).withDefault(8080),
  nested[DbConfig]("db."),         // strips "db." prefix, delegates to dbLoader
  optional[String]("app.desc")     // missing → None, present → Some(value)
)(AppConfig.apply)

// Load — all errors accumulated, not fail-fast
appLoader.load(Map(
  "app.name" -> "my-service",
  "db.url"   -> "jdbc:pg://localhost/db"
))
// Right(AppConfig("my-service", 8080, DbConfig("jdbc:pg://localhost/db", 10), None))
```

**Field types:**

| DSL method | Behavior |
|-----------|----------|
| `field[T]("key")` | Required, fails if missing (unless `.withDefault(v)`) |
| `field[T]("key", rule)` | Required with validation rule |
| `optional[T]("key")` | `None` if missing, `Some(v)` if present |
| `optional[T]("key", rule)` | Validates only when present |
| `nested[T]("prefix.")` | Delegates to implicit `ConfigLoader[T]`, strips prefix |
| `listField[T]("key")` | Parses HOCON inline (`[a, b]`), comma-separated, or indexed keys (`key.0`, `key.1`) |
| `listField[T]("key", rule)` | List with per-element validation rule |

**Supported types:** `String`, `Int`, `Long`, `Double`, `Float`, `Boolean`, `Short`, `Byte`, plus `URL`, `URI`, `Path`, `Duration`, `LocalDate`, `LocalDateTime`, `Instant`, `BigDecimal`, `BigInteger`.

**Error accumulation:** All fields are validated independently. If multiple fields fail, all errors are returned:

```scala
appLoader.load(Map("app.name" -> "", "app.port" -> "99999", "db.url" -> "bad")) match {
  case Left(errors) => errors.foreach(e => println(s"[${e.propertyName}] ${e.message}"))
  // [app.name] Required property 'app.name' is missing
  // [app.port] Value must be between 1 and 65535
  // [db.url] Value must start with: jdbc:
  case Right(config) => // use config
}
```

See [ConfigLoaderExample.scala](../cleanconfig-examples/src/main/scala/com/cleanconfig/examples/scala/ConfigLoaderExample.scala) for more examples.

## Property DSL (Fine-Grained API)

The Property DSL gives fine-grained control over individual property definitions, registries, and validators:

```scala
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
│  - ConfigLoader (case class binding)    │
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
│  - ConfigLoader / FieldDef / FieldType  │
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
│  - TypeConverterRegistry               │
│  - PropertyRegistry                     │
└─────────────────────────────────────────┘
```

## Available Validation Rules

### String Rules
`notBlank`, `minLength(n)`, `maxLength(n)`, `pattern(regex)`, `email`, `url`, `startsWith(prefix)`, `endsWith(suffix)`, `contains(substring)`

### Numeric Rules
`positive`, `negative`, `nonNegative`, `integerBetween(min, max)`, `longBetween(min, max)`, `between(min, max)`, `port`

### File System Rules
`fileExists`, `directoryExists`, `readable`, `writable`, `isDirectory`, `isFile`

### Custom Rules
```scala
val evenRule = Rules.custom[Int]("Must be even")(n => n % 2 == 0)
```

See [Validation Rules Reference](../docs/validation-rules.md) for the full catalog (40+ rules).

## Examples

- [ConfigLoaderExample.scala](../cleanconfig-examples/src/main/scala/com/cleanconfig/examples/scala/ConfigLoaderExample.scala) — Case class binding with nested, optional, defaults, and error accumulation
- [ScalaDslExample.scala](../cleanconfig-examples/src/main/scala/com/cleanconfig/examples/scala/ScalaDslExample.scala) — Property DSL with registry and validator

Run examples:
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.scala.ConfigLoaderExample
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.scala.ScalaDslExample
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
- **CleanConfig Core**: 0.3.0-SNAPSHOT

## Documentation

- [Scala Integration Guide](../docs/scala-integration.md) - Framework integrations, best practices, advanced patterns
- [Validation Rules](../docs/validation-rules.md) - Complete validation rules reference
- [Core Module](../cleanconfig-core/README.md) - Core CleanConfig concepts
- [Developer Guide](../docs/developer-guide.md) - Build, test, and contribute

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

Apache License 2.0 - see [LICENSE](../LICENSE) file for details.
