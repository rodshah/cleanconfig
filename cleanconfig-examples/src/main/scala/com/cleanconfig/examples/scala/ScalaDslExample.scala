package com.cleanconfig.examples.scala

import com.cleanconfig.scala._
import com.cleanconfig.scala.RuleOps._
import com.cleanconfig.core.PropertyCategory

/**
 * Comprehensive example demonstrating the Scala DSL for CleanConfig.
 *
 * Shows:
 * - Type-safe property definitions with named parameters
 * - Idiomatic validation rule composition using && and || operators
 * - Scala collections and Option types
 * - PropertyRegistry builder pattern
 * - Validation with detailed error messages
 */
object ScalaDslExample extends App {

  implicit val stringClass: Class[String] = classOf[String]
  implicit val integerClass: Class[Integer] = classOf[Integer]
  implicit val boolClass: Class[Boolean] = classOf[Boolean]

  println("=== CleanConfig Scala DSL Example ===\n")

  // ===== Define Properties with Scala DSL =====

  val serverPort = Property[Integer](
    name = "server.port",
    defaultValue = Some(8080),
    validationRule = Some(Rules.port),
    description = Some("HTTP server port number"),
    category = PropertyCategory.NETWORKING
  )

  val appName = Property[String](
    name = "app.name",
    validationRule = Some(Rules.notBlank && Rules.minLength(3) && Rules.maxLength(50)),
    description = Some("Application name (3-50 characters)"),
    category = PropertyCategory.GENERAL
  )

  val adminEmail = Property[String](
    name = "admin.email",
    validationRule = Some(Rules.notBlank && Rules.email && Rules.endsWith("@company.com")),
    description = Some("Administrator email (must be @company.com)"),
    category = PropertyCategory.SECURITY
  )

  val dbUrl = Property[String](
    name = "database.url",
    validationRule = Some(Rules.notBlank && (Rules.startsWith("jdbc:postgresql://") || Rules.startsWith("jdbc:mysql://"))),
    description = Some("Database JDBC URL (PostgreSQL or MySQL)"),
    category = PropertyCategory.DATABASE
  )

  val maxConnections = Property[Integer](
    name = "database.max.connections",
    defaultValue = Some(20),
    validationRule = Some(Rules.integerBetween(1, 100)),
    description = Some("Maximum database connections (1-100)"),
    category = PropertyCategory.DATABASE
  )

  val debugMode = Property[Boolean](
    name = "debug.enabled",
    defaultValue = Some(false),
    description = Some("Enable debug logging"),
    category = PropertyCategory.GENERAL
  )

  // Custom validation rule
  val apiKey = Property[String](
    name = "api.key",
    validationRule = Some(
      Rules.notBlank &&
      Rules.minLength(32) &&
      Rules.custom[String]("API key must be alphanumeric")(key => key.matches("^[a-zA-Z0-9]+$"))
    ),
    description = Some("API key for external service (32+ alphanumeric chars)"),
    category = PropertyCategory.SECURITY
  )

  // ===== Build Registry =====

  val registry = PropertyRegistry()
    .registerAll(
      serverPort,
      appName,
      adminEmail,
      dbUrl,
      maxConnections,
      debugMode,
      apiKey
    )
    .build()

  println(s"Registered ${registry.allProperties.size} properties\n")

  // ===== Example 1: Valid Configuration =====

  println("Example 1: Valid Configuration")
  println("--------------------------------")

  val validConfig = Map(
    "server.port" -> "8080",
    "app.name" -> "My Scala Application",
    "admin.email" -> "admin@company.com",
    "database.url" -> "jdbc:postgresql://localhost:5432/mydb",
    "database.max.connections" -> "25",
    "debug.enabled" -> "false",
    "api.key" -> "abcdef1234567890abcdef1234567890"
  )

  val validator = PropertyValidator(registry)
  val validResult = validator.validate(validConfig)

  if (validResult.isValid) {
    println("✓ Validation passed!")
    println(s"  Validated ${registry.allProperties.size} properties")
  } else {
    println("✗ Validation failed:")
    validResult.errors.foreach { error =>
      println(s"  - ${error.propertyName}: ${error.message}")
    }
  }
  println()

  // ===== Example 2: Invalid Configuration =====

  println("Example 2: Invalid Configuration")
  println("---------------------------------")

  val invalidConfig = Map(
    "server.port" -> "99999",                           // Invalid: port out of range
    "app.name" -> "AB",                                 // Invalid: too short
    "admin.email" -> "admin@wrong.com",                 // Invalid: wrong domain
    "database.url" -> "jdbc:h2:mem:testdb",            // Invalid: unsupported database
    "database.max.connections" -> "200",                // Invalid: exceeds maximum
    "api.key" -> "short"                                // Invalid: too short
  )

  val invalidResult = validator.validate(invalidConfig)

  if (!invalidResult.isValid) {
    println("✗ Validation failed (as expected):\n")
    invalidResult.errors.foreach { error =>
      println(s"Property: ${error.propertyName}")
      println(s"  Message: ${error.message}")
      error.actualValue.foreach(v => println(s"  Actual: $v"))
      error.expectedValue.foreach(v => println(s"  Expected: $v"))
      error.suggestion.foreach(s => println(s"  Suggestion: $s"))
      println()
    }
  }

  // ===== Example 3: All Properties Query =====

  println("Example 3: All Properties Query")
  println("--------------------------------")

  println(s"Total properties registered: ${registry.allProperties.size}")
  registry.allProperties.foreach { prop =>
    val desc = if (prop.getDescription.isPresent) prop.getDescription.get else "No description"
    println(s"  - ${prop.getName}: $desc")
  }
  println()

  // ===== Example 4: Error Filtering =====

  println("Example 4: Filtered Error Messages")
  println("-----------------------------------")

  val portErrors = invalidResult.errorsFor("server.port")
  if (portErrors.nonEmpty) {
    println(s"Errors for 'server.port': ${portErrors.head.message}")
  }

  val emailErrors = invalidResult.errorsFor("admin.email")
  if (emailErrors.nonEmpty) {
    println(s"Errors for 'admin.email': ${emailErrors.head.message}")
  }
  println()

  // ===== Example 5: Scala-friendly Query API =====

  println("Example 5: Property Query API")
  println("------------------------------")

  registry.getProperty("server.port") match {
    case Some(prop) =>
      println(s"✓ Found property: ${prop.getName}")
      println(s"  Category: ${prop.getCategory.name()}")
      if (prop.getDefaultValue.isPresent) {
        // ConditionalDefaultValue needs context to compute the actual value
        val context = PropertyContext(Map.empty[String, String])
        val defaultValue = prop.getDefaultValue.get.computeDefault(context.underlying)
        if (defaultValue.isPresent) {
          println(s"  Default: ${defaultValue.get}")
        }
      }
    case None =>
      println("✗ Property not found")
  }
  println()

  println("=== Example Complete ===")
}
