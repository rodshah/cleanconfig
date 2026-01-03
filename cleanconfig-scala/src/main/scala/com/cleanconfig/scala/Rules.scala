package com.cleanconfig.scala

import com.cleanconfig.core.validation.{Rules => JavaRules, ValidationRule, ValidationResult, ValidationError, Conditions => JavaConditions}
import com.cleanconfig.core.PropertyContext
import scala.jdk.CollectionConverters._

/**
 * Scala-friendly wrapper for ValidationRule with operator overloading.
 *
 * Provides idiomatic Scala composition operators:
 * - `rule1 && rule2` for AND composition
 * - `rule1 || rule2` for OR composition
 * - `rule.when(condition)` for conditional execution
 *
 * Example:
 * {{{
 * val emailRule = Rules.notBlank && Rules.email && Rules.endsWith("@company.com")
 * val portRule = Rules.positive && Rules.integerBetween(1, 65535)
 * }}}
 */
object RuleOps {

  implicit class ValidationRuleOps[T](val rule: ValidationRule[T]) extends AnyVal {

    /**
     * AND composition operator.
     *
     * @param other the rule to compose with
     * @return a rule that passes only if both rules pass
     */
    def &&(other: ValidationRule[T]): ValidationRule[T] = rule.and(other)

    /**
     * OR composition operator.
     *
     * @param other the rule to compose with
     * @return a rule that passes if either rule passes
     */
    def ||(other: ValidationRule[T]): ValidationRule[T] = rule.or(other)

    /**
     * Conditional execution.
     *
     * @param condition predicate to check
     * @return a rule that only runs if the condition is true
     */
    def when(condition: => Boolean): ValidationRule[T] =
      rule.onlyIf(_ => condition)
  }
}

/**
 * Scala-friendly access to validation rules.
 *
 * Provides all rules from the Java Rules class with Scala-friendly syntax.
 * Import RuleOps for operator overloading:
 *
 * {{{
 * import com.cleanconfig.scala.Rules._
 * import com.cleanconfig.scala.RuleOps._
 *
 * val rule = notBlank && minLength(3) && maxLength(50)
 * }}}
 */
object Rules {

  // ===== General Rules =====

  /** Property must be present (not null). */
  def required[T]: ValidationRule[T] = JavaRules.required()

  /** Value must be one of the specified options. */
  def oneOf[T](values: T*): ValidationRule[T] =
    JavaRules.oneOf(values.asJava)

  // ===== String Rules =====

  /** String must not be null, empty, or whitespace-only. */
  def notBlank: ValidationRule[String] = JavaRules.notBlank()

  /** String length must be at least the specified minimum. */
  def minLength(min: Int): ValidationRule[String] = JavaRules.minLength(min)

  /** String length must not exceed the specified maximum. */
  def maxLength(max: Int): ValidationRule[String] = JavaRules.maxLength(max)

  /** String must match the specified regular expression. */
  def matchesRegex(regex: String): ValidationRule[String] = JavaRules.matchesRegex(regex)

  /** Alias for matchesRegex. */
  def pattern(regex: String): ValidationRule[String] = matchesRegex(regex)

  /** String must be a valid email address. */
  def email: ValidationRule[String] = JavaRules.email()

  /** String must be a valid URL. */
  def url: ValidationRule[String] = JavaRules.url()

  /** String must start with the specified prefix. */
  def startsWith(prefix: String): ValidationRule[String] = JavaRules.startsWith(prefix)

  /** String must end with the specified suffix. */
  def endsWith(suffix: String): ValidationRule[String] = JavaRules.endsWith(suffix)

  /** String must contain the specified substring. */
  def contains(substring: String): ValidationRule[String] = JavaRules.contains(substring)

  // ===== Numeric Rules =====

  /** Number must be positive (> 0). */
  def positive: ValidationRule[Number] = JavaRules.positive()

  /** Number must be non-negative (>= 0). */
  def nonNegative: ValidationRule[Number] = JavaRules.nonNegative()

  /** Number must be negative (< 0). */
  def negative: ValidationRule[Number] = JavaRules.negative()

  /** Integer must be within the specified range (inclusive). */
  def integerBetween(min: Int, max: Int): ValidationRule[Integer] =
    JavaRules.integerBetween(min, max)

  /** Long must be within the specified range (inclusive). */
  def longBetween(min: Long, max: Long): ValidationRule[java.lang.Long] =
    JavaRules.longBetween(min, max)

  /** Number must be within the specified range (inclusive). */
  def between(min: Double, max: Double): ValidationRule[Number] =
    JavaRules.between(min, max)

  /** Integer must be a valid port number (1-65535). */
  def port: ValidationRule[Integer] = JavaRules.port()

  // ===== File System Rules =====

  /** File/path must exist in the file system. */
  def fileExists: ValidationRule[String] = JavaRules.fileExists()

  /** Directory must exist. */
  def directoryExists: ValidationRule[String] = JavaRules.directoryExists()

  /** File/path must be readable. */
  def readable: ValidationRule[String] = JavaRules.readable()

  /** File/path must be writable. */
  def writable: ValidationRule[String] = JavaRules.writable()

  /** Path must be a directory. */
  def isDirectory: ValidationRule[String] = JavaRules.isDirectory()

  /** Path must be a regular file (not a directory). */
  def isFile: ValidationRule[String] = JavaRules.isFile()

  // ===== Custom Rules =====

  /**
   * Creates a custom validation rule with a predicate function.
   *
   * @param message error message if validation fails
   * @param predicate validation function
   * @tparam T the property type
   * @return validation rule
   */
  def custom[T](message: String)(predicate: T => Boolean): ValidationRule[T] = {
    new ValidationRule[T] {
      override def validate(propertyName: String, value: T, context: PropertyContext): ValidationResult = {
        if (predicate(value)) {
          ValidationResult.success()
        } else {
          ValidationResult.failure(
            ValidationError.builder()
              .propertyName(propertyName)
              .errorMessage(message)
              .actualValue(value.toString)
              .build()
          )
        }
      }
    }
  }
}

/**
 * Scala-friendly access to validation conditions.
 *
 * Used with `rule.onlyIf(condition)` for conditional validation.
 *
 * Example:
 * {{{
 * val httpsRule = Rules.urlWithProtocol("https")
 *   .onlyIf(Conditions.propertyEquals("secure.mode", "true"))
 * }}}
 */
object Conditions {

  /** Condition that checks if a property equals a specific value. */
  def propertyEquals(propertyName: String, expectedValue: String) =
    JavaConditions.propertyEquals(propertyName, expectedValue)

  /** Condition that checks if a boolean property is true. */
  def propertyIsTrue(propertyName: String) =
    JavaConditions.propertyIsTrue(propertyName)

  /** Condition that checks if a property is false. */
  def propertyIsFalse(propertyName: String) =
    JavaConditions.propertyIsFalse(propertyName)

  /** Condition that checks if a property is present. */
  def propertyIsPresent(propertyName: String) =
    JavaConditions.propertyIsPresent(propertyName)
}
