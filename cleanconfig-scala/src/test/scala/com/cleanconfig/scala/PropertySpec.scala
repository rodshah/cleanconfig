package com.cleanconfig.scala

import com.cleanconfig.core.PropertyCategory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PropertySpec extends AnyFlatSpec with Matchers {

  "Property.apply" should "create a PropertyDefinition with named parameters" in {
    implicit val intClass: Class[Int] = classOf[Int]

    val prop = Property[Int](
      name = "server.port",
      defaultValue = Some(8080),
      description = Some("Server port number")
    )

    prop.getName shouldBe "server.port"
    prop.getDefaultValue.isPresent shouldBe true

    // ConditionalDefaultValue needs context to compute the value
    val context = PropertyContext(Map.empty[String, String])
    val defaultValue = prop.getDefaultValue.get.computeDefault(context.underlying)
    defaultValue.isPresent shouldBe true
    defaultValue.get shouldBe 8080

    prop.getDescription.isPresent shouldBe true
    prop.getDescription.get shouldBe "Server port number"
  }

  it should "create a property without default value" in {
    implicit val stringClass: Class[String] = classOf[String]

    val prop = Property[String](
      name = "app.name",
      validationRule = Some(Rules.notBlank)
    )

    prop.getName shouldBe "app.name"
    prop.getDefaultValue.isPresent shouldBe false
    prop.getValidationRule.isPresent shouldBe true
  }

  it should "create a property with validation order and dependencies" in {
    implicit val stringClass: Class[String] = classOf[String]

    val prop = Property[String](
      name = "derived.property",
      dependsOn = Seq("base.property"),
      validationOrder = 2
    )

    prop.getName shouldBe "derived.property"
    prop.getDependsOnForValidation should not be empty
    prop.getValidationOrder shouldBe 2
  }

  "Property.required" should "create a required property with validation" in {
    implicit val stringClass: Class[String] = classOf[String]

    val prop = Property.required[String](
      name = "required.field",
      validationRule = Rules.notBlank,
      description = "A required field"
    )

    prop.getName shouldBe "required.field"
    prop.getDefaultValue.isPresent shouldBe false
    prop.getValidationRule.isPresent shouldBe true
    prop.getDescription.isPresent shouldBe true
  }

  "Property.withDefault" should "create a property with default value" in {
    implicit val intClass: Class[Int] = classOf[Int]

    val prop = Property.withDefault[Int](
      name = "max.connections",
      defaultValue = 100,
      category = PropertyCategory.PERFORMANCE
    )

    prop.getName shouldBe "max.connections"
    prop.getDefaultValue.isPresent shouldBe true

    // ConditionalDefaultValue needs context to compute the value
    val context = PropertyContext(Map.empty[String, String])
    val defaultValue = prop.getDefaultValue.get.computeDefault(context.underlying)
    defaultValue.isPresent shouldBe true
    defaultValue.get shouldBe 100

    prop.getCategory shouldBe PropertyCategory.PERFORMANCE
  }

  it should "work with String type" in {
    implicit val stringClass: Class[String] = classOf[String]

    val prop = Property.withDefault[String](
      name = "app.mode",
      defaultValue = "production"
    )

    // ConditionalDefaultValue needs context to compute the value
    val context = PropertyContext(Map.empty[String, String])
    val defaultValue = prop.getDefaultValue.get.computeDefault(context.underlying)
    defaultValue.get shouldBe "production"
  }
}
