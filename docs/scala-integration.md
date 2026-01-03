# Scala Integration

Idiomatic Scala wrapper for CleanConfig with type-safe DSL and functional programming support.

## Installation & Quick Start

See [cleanconfig-scala README](../cleanconfig-scala/README.md) for installation and basic usage.

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
