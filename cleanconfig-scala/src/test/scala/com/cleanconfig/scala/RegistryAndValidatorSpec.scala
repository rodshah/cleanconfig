package com.cleanconfig.scala

import com.cleanconfig.core.PropertyCategory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RegistryAndValidatorSpec extends AnyFlatSpec with Matchers {

  implicit val stringClass: Class[String] = classOf[String]
  implicit val integerClass: Class[Integer] = classOf[Integer]

  "PropertyRegistry" should "register and retrieve properties" in {
    val serverPort = Property[Integer](
      name = "server.port",
      defaultValue = Some(8080),
      validationRule = Some(Rules.port)
    )

    val appName = Property[String](
      name = "app.name",
      validationRule = Some(Rules.notBlank)
    )

    val registry = PropertyRegistry()
      .register(serverPort)
      .register(appName)
      .build()

    registry.allProperties should have size 2
    registry.isDefined("server.port") shouldBe true
    registry.isDefined("app.name") shouldBe true
    registry.isDefined("missing.property") shouldBe false
  }

  it should "get properties by name" in {
    val prop = Property[String](
      name = "test.property",
      defaultValue = Some("value")
    )

    val registry = PropertyRegistry()
      .register(prop)
      .build()

    val retrieved = registry.getProperty("test.property")
    retrieved shouldBe defined
    retrieved.get.getName shouldBe "test.property"

    val missing = registry.getProperty("missing")
    missing shouldBe None
  }

  "PropertyValidator" should "validate properties" in {
    val serverPort = Property[Integer](
      name = "server.port",
      defaultValue = Some(8080),
      validationRule = Some(Rules.port)
    )

    val registry = PropertyRegistry()
      .register(serverPort)
      .build()

    val validator = PropertyValidator(registry)

    val validResult = validator.validate(Map("server.port" -> "8080"))
    validResult.isValid shouldBe true
    validResult.errors shouldBe empty

    val invalidResult = validator.validate(Map("server.port" -> "99999"))
    invalidResult.isValid shouldBe false
    invalidResult.errors should not be empty
    invalidResult.errorsFor("server.port") should not be empty
  }

  it should "provide Scala collections for errors" in {
    val emailProp = Property[String](
      name = "email",
      validationRule = Some(Rules.email)
    )

    val registry = PropertyRegistry()
      .register(emailProp)
      .build()

    val validator = PropertyValidator(registry)
    val result = validator.validate(Map("email" -> "invalid-email"))

    result.isValid shouldBe false
    result.errors shouldBe a[List[_]]
    result.errorMessages shouldBe a[List[_]]
    result.errorMessages should not be empty
  }

  "ValidationResult" should "provide error details" in {
    val prop = Property[Integer](
      name = "port",
      validationRule = Some(Rules.port)
    )

    val registry = PropertyRegistry()
      .register(prop)
      .build()

    val validator = PropertyValidator(registry)
    val result = validator.validate(Map("port" -> "99999"))

    result.isValid shouldBe false
    val errors = result.errorsFor("port")
    errors should not be empty

    val error = errors.head
    error.propertyName shouldBe "port"
    error.message should not be empty
    error.actualValue shouldBe defined
  }

  "PropertyContext" should "provide Scala collections" in {
    val context = PropertyContext(
      properties = Map("key1" -> "value1", "key2" -> "value2"),
      metadata = Map("env" -> "test", "version" -> "1.0")
    )

    context.properties shouldBe a[Map[_, _]]
    context.properties should have size 2

    context.getProperty("key1") shouldBe Some("value1")
    context.getProperty("missing") shouldBe None

    context.getMetadata("env") shouldBe Some("test")
    context.getMetadata("version") shouldBe Some("1.0")
  }
}
