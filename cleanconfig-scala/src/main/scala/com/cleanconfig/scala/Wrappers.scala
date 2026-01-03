package com.cleanconfig.scala

import com.cleanconfig.core.{
  PropertyContext => JavaPropertyContext,
  PropertyRegistry => JavaPropertyRegistry,
  PropertyRegistryBuilder => JavaPropertyRegistryBuilder,
  PropertyDefinition
}
import com.cleanconfig.core.validation.{ValidationResult => JavaValidationResult, ValidationError => JavaValidationError}
import com.cleanconfig.core.impl.{DefaultPropertyContext, DefaultPropertyValidator}

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Scala-friendly wrapper for PropertyContext.
 *
 * Provides Scala collections and Option instead of Java collections and Optional.
 */
case class PropertyContext(underlying: JavaPropertyContext) {

  /** Gets all properties as a Scala Map. */
  def properties: Map[String, String] =
    underlying.getAllProperties.asScala.toMap

  /** Gets a property value as an Option. */
  def getProperty(name: String): Option[String] =
    underlying.getProperty(name).toScala

  /** Gets a typed property value as an Option. */
  def getTypedProperty[T](name: String, typeClass: Class[T]): Option[T] =
    underlying.getTypedProperty(name, typeClass).toScala

  /** Gets a metadata value as an Option. */
  def getMetadata(key: String): Option[String] =
    underlying.getMetadata(key).toScala

  /** Checks if a property is present. */
  def hasProperty(name: String): Boolean =
    underlying.hasProperty(name)
}

object PropertyContext {

  /**
   * Creates a PropertyContext from a Scala Map.
   *
   * @param properties the properties map
   * @param metadata optional context metadata
   * @return PropertyContext instance
   */
  def apply(
      properties: Map[String, String],
      metadata: Map[String, String] = Map.empty
  ): PropertyContext = {
    val converterRegistry = com.cleanconfig.core.converter.TypeConverterRegistry.getInstance()
    PropertyContext(new DefaultPropertyContext(properties.asJava, converterRegistry, metadata.asJava))
  }
}

/**
 * Scala-friendly wrapper for ValidationResult.
 *
 * Provides Scala collections for errors.
 */
case class ValidationResult(underlying: JavaValidationResult) {

  /** True if validation passed. */
  def isValid: Boolean = underlying.isValid

  /** All validation errors as a Scala List. */
  def errors: List[ValidationError] =
    underlying.getErrors.asScala.map(ValidationError(_)).toList

  /** All error messages as a Scala List. */
  def errorMessages: List[String] =
    errors.map(_.message)

  /** Gets errors for a specific property. */
  def errorsFor(propertyName: String): List[ValidationError] =
    errors.filter(_.propertyName == propertyName)
}

/**
 * Scala-friendly wrapper for ValidationError.
 */
case class ValidationError(underlying: JavaValidationError) {

  def propertyName: String = underlying.getPropertyName

  def message: String = underlying.getErrorMessage

  def actualValue: Option[String] = Option(underlying.getActualValue)

  def expectedValue: Option[String] = Option(underlying.getExpectedValue)

  def suggestion: Option[String] = Option(underlying.getSuggestion)

  override def toString: String = underlying.toString
}

/**
 * Scala-friendly builder for PropertyRegistry.
 *
 * Example:
 * {{{
 * val registry = PropertyRegistry()
 *   .register(serverPort)
 *   .register(appName)
 *   .register(dbUrl)
 *   .build()
 * }}}
 */
class PropertyRegistry(val underlying: JavaPropertyRegistry) {

  /** Gets all registered properties as a Scala List. */
  def allProperties: List[PropertyDefinition[_]] =
    underlying.getAllProperties.asScala.toList

  /** Checks if a property is defined. */
  def isDefined(name: String): Boolean =
    underlying.isDefined(name)

  /** Gets a property definition by name. */
  def getProperty(name: String): Option[PropertyDefinition[_]] =
    underlying.getProperty(name).toScala

  /** Gets all property names as a Scala List. */
  def allPropertyNames: List[String] =
    underlying.getAllPropertyNames.asScala.toList
}

object PropertyRegistry {

  /**
   * Creates a new PropertyRegistry builder.
   *
   * @return a new PropertyRegistry instance for chaining
   */
  def apply(): Builder = new Builder(JavaPropertyRegistry.builder())

  /**
   * Builder for PropertyRegistry with method chaining.
   */
  class Builder(underlying: JavaPropertyRegistryBuilder) {

    /**
     * Registers a property definition.
     *
     * @param definition the property definition to register
     * @return this builder for chaining
     */
    def register(definition: PropertyDefinition[_]): Builder = {
      underlying.register(definition.asInstanceOf[PropertyDefinition[Any]])
      this
    }

    /**
     * Registers multiple property definitions.
     *
     * @param definitions the property definitions to register
     * @return this builder for chaining
     */
    def registerAll(definitions: PropertyDefinition[_]*): Builder = {
      definitions.foreach(d => underlying.register(d.asInstanceOf[PropertyDefinition[Any]]))
      this
    }

    /**
     * Builds the PropertyRegistry.
     *
     * @return immutable PropertyRegistry instance
     */
    def build(): PropertyRegistry =
      new PropertyRegistry(underlying.build())
  }
}

/**
 * Scala-friendly wrapper for PropertyValidator.
 */
class PropertyValidator(val underlying: DefaultPropertyValidator) {

  /**
   * Validates a map of properties.
   *
   * @param properties the properties to validate
   * @return validation result
   */
  def validate(properties: Map[String, String]): ValidationResult =
    ValidationResult(underlying.validate(properties.asJava))
}

object PropertyValidator {

  /**
   * Creates a PropertyValidator for the given registry.
   *
   * @param registry the property registry
   * @return PropertyValidator instance
   */
  def apply(registry: PropertyRegistry): PropertyValidator = {
    val javaValidator = new DefaultPropertyValidator(registry.underlying)
    new PropertyValidator(javaValidator)
  }
}
