# Scala Integration

Idiomatic Scala wrapper for CleanConfig with type-safe DSL and functional programming support.

## Installation & Quick Start

See [cleanconfig-scala README](../cleanconfig-scala/README.md) for installation and basic usage.

## ConfigLoader — Case Class Binding

`ConfigLoader[A]` loads a `Map[String, String]` directly into a typed, validated Scala case class. It replaces the manual define-register-validate-extract workflow when your goal is to populate case classes from config.

### Before / After

**Before (manual 4-step workflow):**
```scala
// 1. Define properties
val port = Property[Integer](name = "port", validationRule = Some(Rules.port), defaultValue = Some(8080))
val name = Property[String](name = "name", validationRule = Some(Rules.notBlank))
// 2. Register
val registry = PropertyRegistry().register(port).register(name).build()
// 3. Validate
val result = PropertyValidator(registry).validate(configMap)
// 4. Manually extract
if (result.isValid) ServerConfig(configMap("name"), configMap("port").toInt)
```

**After (ConfigLoader):**
```scala
import com.cleanconfig.scala.ConfigLoader._

case class ServerConfig(name: String, port: Int)

implicit val loader: ConfigLoader[ServerConfig] = ConfigLoader.build(
  field[String]("name", Rules.notBlank),
  field[Int]("port", Rules.port).withDefault(8080)
)(ServerConfig.apply)

val result: Either[List[ValidationError], ServerConfig] = loader.load(configMap)
```

### Field Types

| Method | Behavior |
|--------|----------|
| `field[T]("key")` | Required. Fails if key is missing (unless `.withDefault(v)` is set) |
| `field[T]("key", rule)` | Required with validation |
| `field[T]("key", rule).withDefault(v)` | Uses `v` if key is absent; validates if key is present |
| `optional[T]("key")` | `None` if missing, `Some(v)` if present |
| `optional[T]("key", rule)` | Validates only when present |
| `nested[T]("prefix.")` | Delegates to implicit `ConfigLoader[T]`, strips prefix from keys |
| `listField[T]("key")` | Parses HOCON inline (`[a, b]`), comma-separated, or indexed keys (`key.0`, `key.1`) |
| `listField[T]("key", rule)` | List with per-element validation. Errors reference position: `key[2]` |

### Nested Case Classes

```scala
case class DbConfig(url: String, maxPool: Int)
case class AppConfig(name: String, db: DbConfig)

implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
  field[String]("url", Rules.notBlank),
  field[Int]("max.pool").withDefault(10)
)(DbConfig.apply)

implicit val appLoader: ConfigLoader[AppConfig] = ConfigLoader.build(
  field[String]("app.name", Rules.notBlank),
  nested[DbConfig]("db.")   // "db.url" → "url", "db.max.pool" → "max.pool"
)(AppConfig.apply)
```

Nesting composes to arbitrary depth. Errors from nested loaders are prefixed back (e.g., `db.url`).

### Error Accumulation

All fields are validated independently (applicative, not monadic). If 5 fields fail, you get 5 errors:

```scala
loader.load(badProps) match {
  case Left(errors) => errors.foreach(e => println(s"[${e.propertyName}] ${e.message}"))
  case Right(config) => // use config
}
```

### Supported Types

`String`, `Int`, `Long`, `Double`, `Float`, `Boolean`, `Short`, `Byte`, `URL`, `URI`, `Path`, `Duration`, `Instant`, `LocalDate`, `LocalDateTime`, `BigDecimal`, `BigInteger`.

## Key Differences from Java API

### Property Definition

**Java**:
```java
PropertyDefinition<Integer> port = PropertyDefinition.builder(Integer.class)
    .name("server.port").defaultValue(8080).validationRule(Rules.port()).build();
```

**Scala**:
```scala
implicit val integerClass: Class[Integer] = classOf[Integer]
val port = Property[Integer](name = "server.port", defaultValue = Some(8080), validationRule = Some(Rules.port))
```

### Validation Composition

**Java**:
```java
ValidationRule<String> rule = Rules.notBlank().and(Rules.email()).and(Rules.endsWith("@company.com"));
```

**Scala**:
```scala
import RuleOps._
val rule = Rules.notBlank && Rules.email && Rules.endsWith("@company.com")
```

### Collections

**Java**:
```java
java.util.List<ValidationError> errors = result.getErrors();
java.util.Map<String, String> props = context.getProperties();
```

**Scala**:
```scala
val errors: List[ValidationError] = result.errors
val props: Map[String, String] = context.properties
```

## Integration with Scala Frameworks

### Akka Actors

```scala
import akka.actor.{Actor, Props}

class ConfigValidatorActor(registry: PropertyRegistry) extends Actor {
  val validator = PropertyValidator(registry)

  def receive = {
    case config: Map[String, String] =>
      sender() ! validator.validate(config)
  }
}

val validatorActor = system.actorOf(Props(new ConfigValidatorActor(registry)))
```

### Play Framework

```scala
import play.api.mvc._

class ConfigController @Inject()(cc: ControllerComponents, registry: PropertyRegistry)
  extends AbstractController(cc) {

  val validator = PropertyValidator(registry)

  def validate = Action(parse.json) { request =>
    val config = request.body.as[Map[String, String]]
    val result = validator.validate(config)
    if (result.isValid) Ok(Json.obj("status" -> "valid"))
    else BadRequest(Json.obj("errors" -> result.errorMessages))
  }
}
```

### Cats Effect

```scala
import cats.effect._

def validateIO(config: Map[String, String]): IO[ValidationResult] =
  IO(validator.validate(config))

def validateAndLog(config: Map[String, String]): IO[Unit] =
  validateIO(config).flatMap { result =>
    if (result.isValid) IO.println("✓ Valid")
    else result.errors.traverse_(e => IO.println(s"✗ ${e.message}"))
  }
```

## Functional Patterns

### Validation Pipeline

```scala
def validateAndApply(config: Map[String, String]): Either[List[String], Config] = {
  val result = validator.validate(config)
  if (result.isValid) Right(Config.from(config))
  else Left(result.errorMessages)
}
```

### Error Accumulation

```scala
def validateAll(configs: List[Map[String, String]]): List[ValidationResult] =
  configs.map(validator.validate)

val allValid = validateAll(configList).forall(_.isValid)
```

## Testing with ScalaTest

```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ValidationSpec extends AnyFlatSpec with Matchers {
  implicit val integerClass: Class[Integer] = classOf[Integer]

  "Port validation" should "accept valid ports" in {
    val prop = Property[Integer](name = "port", validationRule = Some(Rules.port))
    val registry = PropertyRegistry().register(prop).build()
    val validator = PropertyValidator(registry)

    validator.validate(Map("port" -> "8080")).isValid shouldBe true
    validator.validate(Map("port" -> "99999")).isValid shouldBe false
  }
}
```

## Best Practices

1. **Declare implicit type classes at module level**:
```scala
object Config {
  implicit val stringClass: Class[String] = classOf[String]
  implicit val integerClass: Class[Integer] = classOf[Integer]
  // ... properties
}
```

2. **Import operators only when composing rules**:
```scala
import RuleOps._
val rule = Rules.notBlank && Rules.email
```

3. **Leverage pattern matching**:
```scala
registry.getProperty("key") match {
  case Some(prop) => // handle
  case None => // handle
}
```

4. **Use for-comprehensions**:
```scala
for {
  result <- validateConfig(config)
  if result.isValid
  app <- startApp(result)
} yield app
```

## See Also

- [Scala Module README](../cleanconfig-scala/README.md) - Installation, API reference, examples
- [Validation Rules](validation-rules.md) - Available validation rules
- [Developer Guide](developer-guide.md) - Build and contribute
