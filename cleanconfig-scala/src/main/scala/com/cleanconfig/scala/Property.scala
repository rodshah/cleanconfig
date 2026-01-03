package com.cleanconfig.scala

import com.cleanconfig.core.{PropertyCategory, PropertyDefinition}
import com.cleanconfig.core.validation.ValidationRule

/**
 * Scala-friendly wrapper for PropertyDefinition with idiomatic builder syntax.
 *
 * Example:
 * {{{
 * val serverPort = Property[Int](
 *   name = "server.port",
 *   defaultValue = Some(8080),
 *   validationRule = Rules.port(),
 *   description = "Server port number"
 * )
 * }}}
 *
 * @tparam T the type of the property value
 */
object Property {

  /**
   * Creates a PropertyDefinition with Scala-friendly named parameters.
   *
   * @param name the property name (required)
   * @param defaultValue optional default value (None if not provided)
   * @param validationRule optional validation rule
   * @param description optional property description
   * @param category property category (defaults to GENERAL)
   * @param dependsOn properties this property depends on for validation
   * @param validationOrder validation execution order
   * @param typeClass class instance for type T (implicit)
   * @tparam T the property type
   * @return PropertyDefinition instance
   */
  def apply[T](
      name: String,
      defaultValue: Option[T] = None,
      validationRule: Option[ValidationRule[T]] = None,
      description: Option[String] = None,
      category: PropertyCategory = PropertyCategory.GENERAL,
      dependsOn: Seq[String] = Seq.empty,
      validationOrder: Int = 0
  )(implicit typeClass: Class[T]): PropertyDefinition[T] = {

    val builder = PropertyDefinition
      .builder(typeClass)
      .name(name)
      .category(category)

    defaultValue.foreach(builder.defaultValue)
    validationRule.foreach(builder.validationRule)
    description.foreach(builder.description)

    if (dependsOn.nonEmpty) {
      builder.dependsOnForValidation(dependsOn: _*)
    }

    if (validationOrder > 0) {
      builder.validationOrder(validationOrder)
    }

    builder.build()
  }

  /**
   * Creates a required property (no default value).
   *
   * @param name the property name
   * @param validationRule validation rule
   * @param description optional description
   * @param category property category
   * @param typeClass class instance for type T (implicit)
   * @tparam T the property type
   * @return PropertyDefinition instance
   */
  def required[T](
      name: String,
      validationRule: ValidationRule[T],
      description: String = "",
      category: PropertyCategory = PropertyCategory.GENERAL
  )(implicit typeClass: Class[T]): PropertyDefinition[T] = {
    apply(
      name = name,
      validationRule = Some(validationRule),
      description = if (description.nonEmpty) Some(description) else None,
      category = category
    )
  }

  /**
   * Creates a property with default value and no validation.
   *
   * @param name the property name
   * @param defaultValue the default value
   * @param description optional description
   * @param category property category
   * @param typeClass class instance for type T (implicit)
   * @tparam T the property type
   * @return PropertyDefinition instance
   */
  def withDefault[T](
      name: String,
      defaultValue: T,
      description: String = "",
      category: PropertyCategory = PropertyCategory.GENERAL
  )(implicit typeClass: Class[T]): PropertyDefinition[T] = {
    apply(
      name = name,
      defaultValue = Some(defaultValue),
      description = if (description.nonEmpty) Some(description) else None,
      category = category
    )
  }
}
