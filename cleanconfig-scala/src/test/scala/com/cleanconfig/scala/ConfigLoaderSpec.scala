package com.cleanconfig.scala

import com.cleanconfig.scala.ConfigLoader._
import com.cleanconfig.scala.RuleOps._
import com.cleanconfig.core.PropertyContext
import com.cleanconfig.core.validation.{
  ValidationRule => JavaValidationRule,
  ValidationResult => JavaValidationResult,
  ValidationError => JavaValidationError
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigLoaderSpec extends AnyFlatSpec with Matchers {

  // =========================================================================
  // Test case classes
  // =========================================================================

  case class SingleString(value: String)
  case class SingleInt(value: Int)
  case class SingleLong(value: Long)
  case class SingleDouble(value: Double)
  case class SingleFloat(value: Float)
  case class SingleBoolean(value: Boolean)
  case class SingleShort(value: Short)
  case class SingleByte(value: Byte)
  case class TwoFields(name: String, port: Int)
  case class ThreeFields(a: String, b: Int, c: Boolean)
  case class AllPrimitives(s: String, i: Int, l: Long, d: Double, f: Float, b: Boolean, sh: Short, by: Byte)
  case class WithDefaults(host: String, port: Int, debug: Boolean)
  case class WithOptional(name: String, description: Option[String])
  case class MultiOptional(a: Option[String], b: Option[Int], c: Option[Boolean])
  case class DbConfig(url: String, maxPool: Int)
  case class CacheConfig(enabled: Boolean, ttlSeconds: Int)
  case class AppConfig(name: String, port: Int, db: DbConfig, desc: Option[String])
  case class DeepNested(app: AppConfig, cache: CacheConfig)
  case class FiveFields(a: String, b: Int, c: Long, d: Double, e: Boolean)

  // =========================================================================
  // 1. Basic field loading — individual types
  // =========================================================================

  "ConfigLoader basic String" should "load a String field" in {
    val loader = ConfigLoader.build(field[String]("name"))(identity)
    loader.load(Map("name" -> "hello")) shouldBe Right("hello")
  }

  it should "preserve whitespace in String values" in {
    val loader = ConfigLoader.build(field[String]("name"))(identity)
    loader.load(Map("name" -> "  spaced  ")) shouldBe Right("  spaced  ")
  }

  it should "handle special characters in String values" in {
    val loader = ConfigLoader.build(field[String]("path"))(identity)
    loader.load(Map("path" -> "/usr/bin/app?q=1&v=2")) shouldBe Right("/usr/bin/app?q=1&v=2")
  }

  "ConfigLoader basic Int" should "load an Int field" in {
    val loader = ConfigLoader.build(field[Int]("port"))(identity)
    loader.load(Map("port" -> "8080")) shouldBe Right(8080)
  }

  it should "handle negative Int" in {
    val loader = ConfigLoader.build(field[Int]("offset"))(identity)
    loader.load(Map("offset" -> "-10")) shouldBe Right(-10)
  }

  it should "handle zero" in {
    val loader = ConfigLoader.build(field[Int]("count"))(identity)
    loader.load(Map("count" -> "0")) shouldBe Right(0)
  }

  it should "handle Int.MaxValue" in {
    val loader = ConfigLoader.build(field[Int]("big"))(identity)
    loader.load(Map("big" -> Int.MaxValue.toString)) shouldBe Right(Int.MaxValue)
  }

  it should "handle Int.MinValue" in {
    val loader = ConfigLoader.build(field[Int]("small"))(identity)
    loader.load(Map("small" -> Int.MinValue.toString)) shouldBe Right(Int.MinValue)
  }

  "ConfigLoader basic Long" should "load a Long field" in {
    val loader = ConfigLoader.build(field[Long]("ts"))(identity)
    loader.load(Map("ts" -> "999999999999")) shouldBe Right(999999999999L)
  }

  it should "handle Long.MaxValue" in {
    val loader = ConfigLoader.build(field[Long]("big"))(identity)
    loader.load(Map("big" -> Long.MaxValue.toString)) shouldBe Right(Long.MaxValue)
  }

  "ConfigLoader basic Double" should "load a Double field" in {
    val loader = ConfigLoader.build(field[Double]("rate"))(identity)
    loader.load(Map("rate" -> "3.14159")) shouldBe Right(3.14159)
  }

  it should "handle negative Double" in {
    val loader = ConfigLoader.build(field[Double]("temp"))(identity)
    loader.load(Map("temp" -> "-40.5")) shouldBe Right(-40.5)
  }

  "ConfigLoader basic Float" should "load a Float field" in {
    val loader = ConfigLoader.build(field[Float]("ratio"))(identity)
    loader.load(Map("ratio" -> "2.5")) shouldBe Right(2.5f)
  }

  "ConfigLoader basic Boolean" should "load true" in {
    val loader = ConfigLoader.build(field[Boolean]("flag"))(identity)
    loader.load(Map("flag" -> "true")) shouldBe Right(true)
  }

  it should "load false" in {
    val loader = ConfigLoader.build(field[Boolean]("flag"))(identity)
    loader.load(Map("flag" -> "false")) shouldBe Right(false)
  }

  "ConfigLoader basic Short" should "load a Short field" in {
    val loader = ConfigLoader.build(field[Short]("code"))(identity)
    loader.load(Map("code" -> "200")) shouldBe Right(200.toShort)
  }

  "ConfigLoader basic Byte" should "load a Byte field" in {
    val loader = ConfigLoader.build(field[Byte]("level"))(identity)
    loader.load(Map("level" -> "7")) shouldBe Right(7.toByte)
  }

  // =========================================================================
  // 2. Multi-field case classes
  // =========================================================================

  "ConfigLoader multi-field" should "load a two-field case class" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port")
    )(TwoFields.apply)

    loader.load(Map("name" -> "app", "port" -> "9090")) shouldBe Right(TwoFields("app", 9090))
  }

  it should "load a three-field case class" in {
    val loader = ConfigLoader.build(
      field[String]("a"),
      field[Int]("b"),
      field[Boolean]("c")
    )(ThreeFields.apply)

    loader.load(Map("a" -> "x", "b" -> "42", "c" -> "true")) shouldBe Right(ThreeFields("x", 42, true))
  }

  it should "load all eight primitive types together" in {
    val loader = ConfigLoader.build(
      field[String]("s"),
      field[Int]("i"),
      field[Long]("l"),
      field[Double]("d"),
      field[Float]("f"),
      field[Boolean]("b"),
      field[Short]("sh"),
      field[Byte]("by")
    )(AllPrimitives.apply)

    loader.load(Map(
      "s" -> "hello", "i" -> "42", "l" -> "999999999999",
      "d" -> "3.14", "f" -> "2.5", "b" -> "true",
      "sh" -> "100", "by" -> "7"
    )) shouldBe Right(AllPrimitives("hello", 42, 999999999999L, 3.14, 2.5f, true, 100, 7))
  }

  it should "load a five-field case class" in {
    val loader = ConfigLoader.build(
      field[String]("a"),
      field[Int]("b"),
      field[Long]("c"),
      field[Double]("d"),
      field[Boolean]("e")
    )(FiveFields.apply)

    loader.load(Map("a" -> "x", "b" -> "1", "c" -> "2", "d" -> "3.0", "e" -> "false")) shouldBe
      Right(FiveFields("x", 1, 2L, 3.0, false))
  }

  it should "ignore extra properties in the map" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port")
    )(TwoFields.apply)

    loader.load(Map("name" -> "app", "port" -> "80", "extra.key" -> "ignored", "another" -> "also-ignored")) shouldBe
      Right(TwoFields("app", 80))
  }

  // =========================================================================
  // 3. Default values
  // =========================================================================

  "ConfigLoader defaults" should "use default when property is missing" in {
    val loader = ConfigLoader.build(
      field[String]("host"),
      field[Int]("port").withDefault(8080),
      field[Boolean]("debug").withDefault(false)
    )(WithDefaults.apply)

    loader.load(Map("host" -> "localhost")) shouldBe Right(WithDefaults("localhost", 8080, false))
  }

  it should "prefer provided value over default" in {
    val loader = ConfigLoader.build(
      field[String]("host"),
      field[Int]("port").withDefault(8080),
      field[Boolean]("debug").withDefault(false)
    )(WithDefaults.apply)

    loader.load(Map("host" -> "0.0.0.0", "port" -> "9090", "debug" -> "true")) shouldBe
      Right(WithDefaults("0.0.0.0", 9090, true))
  }

  it should "use default when property value is empty string" in {
    val loader = ConfigLoader.build(
      field[Int]("port").withDefault(8080)
    )(identity)

    loader.load(Map("port" -> "")) shouldBe Right(8080)
  }

  it should "succeed when all fields have defaults and map is empty" in {
    val loader = ConfigLoader.build(
      field[String]("a").withDefault("x"),
      field[Int]("b").withDefault(42),
      field[Boolean]("c").withDefault(true)
    )((a, b, c) => (a, b, c))

    loader.load(Map.empty) shouldBe Right(("x", 42, true))
  }

  it should "use default for some fields while loading others" in {
    val loader = ConfigLoader.build(
      field[String]("a").withDefault("default-a"),
      field[String]("b"),
      field[String]("c").withDefault("default-c")
    )((a, b, c) => (a, b, c))

    loader.load(Map("b" -> "provided")) shouldBe Right(("default-a", "provided", "default-c"))
  }

  it should "validate provided value even when default exists" in {
    val loader = ConfigLoader.build(
      field[Int]("port", Rules.port).withDefault(8080)
    )(identity)

    // Provided value violates rule, should fail even though a default exists
    val result = loader.load(Map("port" -> "99999"))
    result.isLeft shouldBe true
  }

  it should "not validate the default value itself" in {
    // Default is 0 which fails Rules.port, but since we don't validate defaults, it should succeed
    val loader = ConfigLoader.build(
      field[Int]("port", Rules.port).withDefault(0)
    )(identity)

    loader.load(Map.empty) shouldBe Right(0)
  }

  // =========================================================================
  // 4. Validation rules
  // =========================================================================

  "ConfigLoader rules" should "pass when rule succeeds" in {
    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank)
    )(identity)

    loader.load(Map("name" -> "valid")) shouldBe Right("valid")
  }

  it should "fail when rule is violated" in {
    val loader = ConfigLoader.build(
      field[String]("email", Rules.email)
    )(identity)

    val result = loader.load(Map("email" -> "not-an-email"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "email"
  }

  it should "work with AND-composed rules (&&)" in {
    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank && Rules.minLength(3) && Rules.maxLength(10))
    )(identity)

    loader.load(Map("name" -> "hello")).isRight shouldBe true
    loader.load(Map("name" -> "ab")).isLeft shouldBe true
    loader.load(Map("name" -> "verylongname!")).isLeft shouldBe true
    loader.load(Map("name" -> "   ")).isLeft shouldBe true
  }

  it should "work with OR-composed rules (||)" in {
    val loader = ConfigLoader.build(
      field[String]("url", Rules.startsWith("http://") || Rules.startsWith("https://"))
    )(identity)

    loader.load(Map("url" -> "http://example.com")).isRight shouldBe true
    loader.load(Map("url" -> "https://example.com")).isRight shouldBe true
    loader.load(Map("url" -> "ftp://example.com")).isLeft shouldBe true
  }

  it should "work with cross-type rule (Int field with port rule)" in {
    val loader = ConfigLoader.build(
      field[Int]("port", Rules.port)
    )(identity)

    loader.load(Map("port" -> "8080")).isRight shouldBe true
    loader.load(Map("port" -> "1")).isRight shouldBe true
    loader.load(Map("port" -> "65535")).isRight shouldBe true
    loader.load(Map("port" -> "99999")).isLeft shouldBe true
    loader.load(Map("port" -> "0")).isLeft shouldBe true
  }

  it should "work with cross-type rule (Int field with integerBetween)" in {
    val loader = ConfigLoader.build(
      field[Int]("count", Rules.integerBetween(1, 100))
    )(identity)

    loader.load(Map("count" -> "50")).isRight shouldBe true
    loader.load(Map("count" -> "1")).isRight shouldBe true
    loader.load(Map("count" -> "100")).isRight shouldBe true
    loader.load(Map("count" -> "0")).isLeft shouldBe true
    loader.load(Map("count" -> "101")).isLeft shouldBe true
  }

  it should "work with custom rule" in {
    val evenRule = Rules.custom[Any]("Must be even") {
      case i: java.lang.Integer => i % 2 == 0
      case _ => false
    }

    val loader = ConfigLoader.build(
      field[Int]("num", evenRule)
    )(identity)

    loader.load(Map("num" -> "4")).isRight shouldBe true
    loader.load(Map("num" -> "5")).isLeft shouldBe true
  }

  it should "work with regex pattern rule" in {
    val loader = ConfigLoader.build(
      field[String]("code", Rules.pattern("^[A-Z]{3}-[0-9]{4}$"))
    )(identity)

    loader.load(Map("code" -> "ABC-1234")).isRight shouldBe true
    loader.load(Map("code" -> "abc-1234")).isLeft shouldBe true
    loader.load(Map("code" -> "ABCD-123")).isLeft shouldBe true
  }

  it should "work with oneOf rule" in {
    val loader = ConfigLoader.build(
      field[String]("env", Rules.oneOf("dev", "staging", "prod"))
    )(identity)

    loader.load(Map("env" -> "prod")).isRight shouldBe true
    loader.load(Map("env" -> "qa")).isLeft shouldBe true
  }

  // =========================================================================
  // 5. Error accumulation
  // =========================================================================

  "ConfigLoader error accumulation" should "collect errors from multiple missing required fields" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port"),
      field[String]("host")
    )((n, p, h) => (n, p, h))

    val result = loader.load(Map.empty)
    result.isLeft shouldBe true
    result.left.getOrElse(Nil) should have size 3
  }

  it should "collect both conversion and validation errors in one pass" in {
    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank),
      field[Int]("port")
    )(TwoFields.apply)

    val result = loader.load(Map("name" -> "", "port" -> "not-a-number"))
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors should have size 2
    errors.map(_.propertyName).toSet shouldBe Set("name", "port")
  }

  it should "have error count matching number of failing fields" in {
    val loader = ConfigLoader.build(
      field[String]("a", Rules.email),
      field[String]("b", Rules.email),
      field[String]("c", Rules.email)
    )((a, b, c) => (a, b, c))

    val result = loader.load(Map("a" -> "bad", "b" -> "also-bad", "c" -> "good@example.com"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil) should have size 2
  }

  it should "accumulate missing, conversion, and validation errors together" in {
    val loader = ConfigLoader.build(
      field[String]("a"),             // missing
      field[Int]("b"),                // conversion error
      field[String]("c", Rules.email) // validation error
    )((a, b, c) => (a, b, c))

    val result = loader.load(Map("b" -> "xyz", "c" -> "not-email"))
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors should have size 3
    errors.map(_.propertyName).toSet shouldBe Set("a", "b", "c")
  }

  it should "return Right when all fields are valid after mixed errors in prior call" in {
    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank),
      field[Int]("port", Rules.port)
    )(TwoFields.apply)

    // First call fails
    loader.load(Map("name" -> "", "port" -> "0")).isLeft shouldBe true
    // Second call with valid data succeeds (stateless)
    loader.load(Map("name" -> "app", "port" -> "8080")) shouldBe Right(TwoFields("app", 8080))
  }

  // =========================================================================
  // 6. Type conversion errors
  // =========================================================================

  "ConfigLoader type conversion errors" should "produce clear error for invalid Int" in {
    val loader = ConfigLoader.build(field[Int]("port"))(identity)

    val result = loader.load(Map("port" -> "abc"))
    result.isLeft shouldBe true
    val err = result.left.getOrElse(Nil).head
    err.propertyName shouldBe "port"
    err.message should include("Integer")
    err.actualValue shouldBe Some("abc")
    err.expectedValue shouldBe Some("Integer")
  }

  it should "produce clear error for invalid Long" in {
    val loader = ConfigLoader.build(field[Long]("ts"))(identity)

    val result = loader.load(Map("ts" -> "not-a-long"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "ts"
  }

  it should "produce clear error for invalid Double" in {
    val loader = ConfigLoader.build(field[Double]("rate"))(identity)

    val result = loader.load(Map("rate" -> "abc"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "rate"
  }

  it should "produce clear error for invalid Boolean" in {
    val loader = ConfigLoader.build(field[Boolean]("flag"))(identity)

    val result = loader.load(Map("flag" -> "maybe"))
    result.isLeft shouldBe true
    val err = result.left.getOrElse(Nil).head
    err.propertyName shouldBe "flag"
    err.message should include("Boolean")
  }

  it should "produce clear error for invalid Short" in {
    val loader = ConfigLoader.build(field[Short]("code"))(identity)

    val result = loader.load(Map("code" -> "99999"))
    result.isLeft shouldBe true
  }

  it should "produce clear error for invalid Byte" in {
    val loader = ConfigLoader.build(field[Byte]("level"))(identity)

    val result = loader.load(Map("level" -> "999"))
    result.isLeft shouldBe true
  }

  it should "produce clear error for overflow Int" in {
    val loader = ConfigLoader.build(field[Int]("big"))(identity)

    val result = loader.load(Map("big" -> "99999999999999"))
    result.isLeft shouldBe true
  }

  it should "fail conversion before reaching validation rule" in {
    val loader = ConfigLoader.build(
      field[Int]("port", Rules.port)
    )(identity)

    // "abc" fails conversion; the port rule is never reached
    val result = loader.load(Map("port" -> "abc"))
    result.isLeft shouldBe true
    val err = result.left.getOrElse(Nil).head
    err.message should include("convert")
  }

  // =========================================================================
  // 7. Missing required fields
  // =========================================================================

  "ConfigLoader missing fields" should "fail for missing required field" in {
    val loader = ConfigLoader.build(field[String]("name"))(identity)

    val result = loader.load(Map.empty)
    result.isLeft shouldBe true
    val err = result.left.getOrElse(Nil).head
    err.propertyName shouldBe "name"
    err.message should include("missing")
  }

  it should "treat empty string as missing for required field" in {
    val loader = ConfigLoader.build(field[String]("name"))(identity)

    val result = loader.load(Map("name" -> ""))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.message should include("missing")
  }

  it should "fail for missing field without default but succeed with default" in {
    val loader = ConfigLoader.build(
      field[String]("required"),
      field[String]("defaulted").withDefault("fallback")
    )((a, b) => (a, b))

    val result = loader.load(Map.empty)
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors should have size 1
    errors.head.propertyName shouldBe "required"
  }

  // =========================================================================
  // 8. Optional fields
  // =========================================================================

  "ConfigLoader optional" should "return None when property is missing" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      optional[String]("desc")
    )(WithOptional.apply)

    loader.load(Map("name" -> "app")) shouldBe Right(WithOptional("app", None))
  }

  it should "return Some when property is present" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      optional[String]("desc")
    )(WithOptional.apply)

    loader.load(Map("name" -> "app", "desc" -> "A service")) shouldBe
      Right(WithOptional("app", Some("A service")))
  }

  it should "validate and fail when present value violates rule" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      optional[String]("desc", Rules.minLength(10))
    )(WithOptional.apply)

    val result = loader.load(Map("name" -> "app", "desc" -> "short"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "desc"
  }

  it should "skip validation when property is missing (even with rule)" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      optional[String]("desc", Rules.minLength(10))
    )(WithOptional.apply)

    loader.load(Map("name" -> "app")) shouldBe Right(WithOptional("app", None))
  }

  it should "treat empty string as missing for optional field" in {
    val loader = ConfigLoader.build(optional[String]("desc"))(identity)
    loader.load(Map("desc" -> "")) shouldBe Right(None)
  }

  it should "handle multiple optional fields with mixed presence" in {
    val loader = ConfigLoader.build(
      optional[String]("a"),
      optional[Int]("b"),
      optional[Boolean]("c")
    )(MultiOptional.apply)

    loader.load(Map("b" -> "42")) shouldBe Right(MultiOptional(None, Some(42), None))
    loader.load(Map.empty) shouldBe Right(MultiOptional(None, None, None))
    loader.load(Map("a" -> "x", "b" -> "1", "c" -> "true")) shouldBe
      Right(MultiOptional(Some("x"), Some(1), Some(true)))
  }

  it should "fail conversion for optional field with invalid value" in {
    val loader = ConfigLoader.build(optional[Int]("count"))(identity)

    val result = loader.load(Map("count" -> "not-int"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "count"
  }

  it should "accumulate errors from multiple failing optional fields" in {
    val loader = ConfigLoader.build(
      optional[Int]("a"),
      optional[Int]("b"),
      optional[Int]("c")
    )((a, b, c) => (a, b, c))

    val result = loader.load(Map("a" -> "bad", "b" -> "also-bad", "c" -> "3"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil) should have size 2
  }

  // =========================================================================
  // 9. Nested case classes
  // =========================================================================

  "ConfigLoader nested" should "load nested case class with prefix" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url", Rules.notBlank),
      field[Int]("max.pool").withDefault(10)
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("app.name"),
      field[Int]("app.port"),
      nested[DbConfig]("db."),
      optional[String]("app.desc")
    )(AppConfig.apply)

    val result = loader.load(Map(
      "app.name" -> "svc",
      "app.port" -> "9090",
      "db.url" -> "jdbc:pg://localhost/mydb",
      "db.max.pool" -> "20"
    ))

    result shouldBe Right(AppConfig("svc", 9090, DbConfig("jdbc:pg://localhost/mydb", 20), None))
  }

  it should "use nested defaults when nested properties are missing" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url").withDefault("jdbc:h2:mem:default"),
      field[Int]("max.pool").withDefault(10)
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[DbConfig]("db.")
    )((n, d) => (n, d))

    loader.load(Map("name" -> "app")) shouldBe Right(("app", DbConfig("jdbc:h2:mem:default", 10)))
  }

  it should "accumulate errors from nested loader" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url", Rules.notBlank),
      field[Int]("max.pool", Rules.integerBetween(1, 100))
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[DbConfig]("db.")
    )((n, d) => (n, d))

    val result = loader.load(Map("name" -> "app", "db.max.pool" -> "999"))
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors should have size 2
  }

  it should "prefix error property names from nested loader" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url"),
      field[Int]("max.pool")
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[DbConfig]("db.")
    )((n, d) => (n, d))

    val result = loader.load(Map("name" -> "app"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).map(_.propertyName).toSet shouldBe Set("db.url", "db.max.pool")
  }

  it should "work with empty prefix (pass full map)" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("db.url"),
      field[Int]("db.max.pool")
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[DbConfig]()
    )((n, d) => (n, d))

    loader.load(Map("name" -> "app", "db.url" -> "jdbc:h2:mem", "db.max.pool" -> "5")) shouldBe
      Right(("app", DbConfig("jdbc:h2:mem", 5)))
  }

  it should "support multiple nested fields in same loader" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url"),
      field[Int]("max.pool").withDefault(10)
    )(DbConfig.apply)

    implicit val cacheLoader: ConfigLoader[CacheConfig] = ConfigLoader.build(
      field[Boolean]("enabled").withDefault(false),
      field[Int]("ttl.seconds").withDefault(300)
    )(CacheConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[DbConfig]("db."),
      nested[CacheConfig]("cache.")
    )((name, db, cache) => (name, db, cache))

    val result = loader.load(Map(
      "name" -> "app",
      "db.url" -> "jdbc:pg://localhost/db",
      "cache.enabled" -> "true",
      "cache.ttl.seconds" -> "600"
    ))

    result shouldBe Right(("app", DbConfig("jdbc:pg://localhost/db", 10), CacheConfig(true, 600)))
  }

  it should "support deeply nested case classes (3 levels)" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url").withDefault("jdbc:h2:mem"),
      field[Int]("max.pool").withDefault(10)
    )(DbConfig.apply)

    implicit val cacheLoader: ConfigLoader[CacheConfig] = ConfigLoader.build(
      field[Boolean]("enabled").withDefault(true),
      field[Int]("ttl.seconds").withDefault(300)
    )(CacheConfig.apply)

    implicit val appLoader: ConfigLoader[AppConfig] = ConfigLoader.build(
      field[String]("name", Rules.notBlank),
      field[Int]("port", Rules.port).withDefault(8080),
      nested[DbConfig]("db."),
      optional[String]("desc")
    )(AppConfig.apply)

    val loader = ConfigLoader.build(
      nested[AppConfig]("app."),
      nested[CacheConfig]("cache.")
    )(DeepNested.apply)

    val result = loader.load(Map(
      "app.name" -> "deep-svc",
      "app.port" -> "9090",
      "app.db.url" -> "jdbc:pg://localhost/deep",
      "cache.enabled" -> "false"
    ))

    result shouldBe Right(DeepNested(
      AppConfig("deep-svc", 9090, DbConfig("jdbc:pg://localhost/deep", 10), None),
      CacheConfig(false, 300)
    ))
  }

  it should "accumulate errors from deeply nested loaders with correct prefixes" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url"),
      field[Int]("max.pool")
    )(DbConfig.apply)

    implicit val appLoader: ConfigLoader[AppConfig] = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port"),
      nested[DbConfig]("db."),
      optional[String]("desc")
    )(AppConfig.apply)

    implicit val cacheLoader: ConfigLoader[CacheConfig] = ConfigLoader.build(
      field[Boolean]("enabled"),
      field[Int]("ttl.seconds")
    )(CacheConfig.apply)

    val loader = ConfigLoader.build(
      nested[AppConfig]("app."),
      nested[CacheConfig]("cache.")
    )(DeepNested.apply)

    val result = loader.load(Map.empty)
    result.isLeft shouldBe true
    val errorProps = result.left.getOrElse(Nil).map(_.propertyName).toSet
    errorProps should contain("app.name")
    errorProps should contain("app.port")
    errorProps should contain("app.db.url")
    errorProps should contain("app.db.max.pool")
    errorProps should contain("cache.enabled")
    errorProps should contain("cache.ttl.seconds")
  }

  it should "preserve suggestion and errorCode from nested errors after re-prefixing" in {
    // Custom rule that sets errorCode and suggestion on failure
    val ruleWithExtras: JavaValidationRule[Any] = new JavaValidationRule[Any] {
      override def validate(propertyName: String, value: Any, context: PropertyContext): JavaValidationResult = {
        JavaValidationResult.failure(
          JavaValidationError.builder()
            .propertyName(propertyName)
            .errorMessage("Bad value")
            .actualValue(value.toString)
            .errorCode("ERR_CUSTOM_001")
            .suggestion("Try using a valid JDBC URL")
            .build()
        )
      }
    }

    case class Inner(url: String)

    implicit val innerLoader: ConfigLoader[Inner] = ConfigLoader.build(
      field[String]("url", ruleWithExtras)
    )(Inner.apply)

    val loader = ConfigLoader.build(
      nested[Inner]("db.")
    )(identity)

    val result = loader.load(Map("db.url" -> "bad-value"))
    result.isLeft shouldBe true

    val err = result.left.getOrElse(Nil).head
    err.propertyName shouldBe "db.url"
    err.message shouldBe "Bad value"
    err.actualValue shouldBe Some("bad-value")
    err.suggestion shouldBe Some("Try using a valid JDBC URL")
    Option(err.underlying.getErrorCode) shouldBe Some("ERR_CUSTOM_001")
  }

  // =========================================================================
  // 10. Complex integration
  // =========================================================================

  "ConfigLoader integration" should "load full AppConfig with all field types" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url", Rules.notBlank && Rules.startsWith("jdbc:")),
      field[Int]("max.pool", Rules.integerBetween(1, 100)).withDefault(10)
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("app.name", Rules.notBlank),
      field[Int]("app.port", Rules.port).withDefault(8080),
      nested[DbConfig]("db."),
      optional[String]("app.desc")
    )(AppConfig.apply)

    val props = Map(
      "app.name" -> "my-service",
      "app.port" -> "9090",
      "db.url" -> "jdbc:postgresql://localhost/mydb",
      "db.max.pool" -> "25",
      "app.desc" -> "Production service"
    )

    loader.load(props) shouldBe Right(
      AppConfig("my-service", 9090, DbConfig("jdbc:postgresql://localhost/mydb", 25), Some("Production service"))
    )
  }

  it should "accumulate errors across all field types simultaneously" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url", Rules.notBlank && Rules.startsWith("jdbc:")),
      field[Int]("max.pool", Rules.integerBetween(1, 100))
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("app.name", Rules.notBlank),
      field[Int]("app.port", Rules.port),
      nested[DbConfig]("db."),
      optional[String]("app.desc", Rules.minLength(10))
    )(AppConfig.apply)

    val props = Map(
      "app.name" -> "",          // fails notBlank
      "app.port" -> "99999",     // fails port rule
      "db.url" -> "bad-url",     // fails startsWith("jdbc:")
      "db.max.pool" -> "999",    // fails integerBetween(1, 100)
      "app.desc" -> "short"      // fails minLength(10)
    )

    val result = loader.load(props)
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors.size should be >= 5
    errors.map(_.propertyName).toSet should contain allOf(
      "app.name", "app.port", "db.url", "db.max.pool", "app.desc"
    )
  }

  it should "load with all defaults and no properties" in {
    implicit val dbLoader: ConfigLoader[DbConfig] = ConfigLoader.build(
      field[String]("url").withDefault("jdbc:h2:mem:test"),
      field[Int]("max.pool").withDefault(5)
    )(DbConfig.apply)

    val loader = ConfigLoader.build(
      field[String]("app.name").withDefault("default-app"),
      field[Int]("app.port").withDefault(8080),
      nested[DbConfig]("db."),
      optional[String]("app.desc")
    )(AppConfig.apply)

    loader.load(Map.empty) shouldBe Right(
      AppConfig("default-app", 8080, DbConfig("jdbc:h2:mem:test", 5), None)
    )
  }

  // =========================================================================
  // 11. Summoner and implicit resolution
  // =========================================================================

  "ConfigLoader summoner" should "resolve via apply[A]" in {
    implicit val loader: ConfigLoader[TwoFields] = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port")
    )(TwoFields.apply)

    ConfigLoader[TwoFields].load(Map("name" -> "a", "port" -> "1")) shouldBe Right(TwoFields("a", 1))
  }

  // =========================================================================
  // 12. Non-primitive types
  // =========================================================================

  "ConfigLoader non-primitive types" should "support java.net.URL" in {
    import java.net.URL
    val loader = ConfigLoader.build(field[URL]("endpoint"))(identity)

    val result = loader.load(Map("endpoint" -> "https://example.com/api"))
    result.isRight shouldBe true
    result.toOption.get.toString shouldBe "https://example.com/api"
  }

  it should "fail for invalid URL" in {
    import java.net.URL
    val loader = ConfigLoader.build(field[URL]("endpoint"))(identity)

    loader.load(Map("endpoint" -> "not a url")).isLeft shouldBe true
  }

  it should "support java.net.URI" in {
    import java.net.URI
    val loader = ConfigLoader.build(field[URI]("uri"))(identity)

    val result = loader.load(Map("uri" -> "https://example.com/path?q=1"))
    result.isRight shouldBe true
    result.toOption.get.getHost shouldBe "example.com"
  }

  it should "support java.time.Duration" in {
    import java.time.Duration
    val loader = ConfigLoader.build(field[Duration]("timeout"))(identity)

    loader.load(Map("timeout" -> "PT30S")) shouldBe Right(Duration.ofSeconds(30))
    loader.load(Map("timeout" -> "PT5M")) shouldBe Right(Duration.ofMinutes(5))
  }

  it should "fail for invalid Duration" in {
    import java.time.Duration
    val loader = ConfigLoader.build(field[Duration]("timeout"))(identity)

    loader.load(Map("timeout" -> "30 seconds")).isLeft shouldBe true
  }

  it should "support java.time.LocalDate" in {
    import java.time.LocalDate
    val loader = ConfigLoader.build(field[LocalDate]("date"))(identity)

    loader.load(Map("date" -> "2024-12-25")) shouldBe Right(LocalDate.of(2024, 12, 25))
  }

  it should "support java.nio.file.Path" in {
    import java.nio.file.{Path, Paths}
    val loader = ConfigLoader.build(field[Path]("dir"))(identity)

    val result = loader.load(Map("dir" -> "/tmp/data"))
    result.isRight shouldBe true
    result.toOption.get shouldBe Paths.get("/tmp/data")
  }

  it should "support java.math.BigDecimal" in {
    import java.math.BigDecimal
    val loader = ConfigLoader.build(field[BigDecimal]("amount"))(identity)

    val result = loader.load(Map("amount" -> "123456.789"))
    result.isRight shouldBe true
    result.toOption.get shouldBe new BigDecimal("123456.789")
  }

  // =========================================================================
  // 13. Error message quality
  // =========================================================================

  "ConfigLoader error messages" should "include property name in missing error" in {
    val loader = ConfigLoader.build(field[String]("my.config.key"))(identity)
    val err = loader.load(Map.empty).left.getOrElse(Nil).head
    err.propertyName shouldBe "my.config.key"
    err.message should include("my.config.key")
  }

  it should "include actual value in conversion error" in {
    val loader = ConfigLoader.build(field[Int]("port"))(identity)
    val err = loader.load(Map("port" -> "garbage")).left.getOrElse(Nil).head
    err.actualValue shouldBe Some("garbage")
  }

  it should "include expected type in conversion error" in {
    val loader = ConfigLoader.build(field[Int]("port"))(identity)
    val err = loader.load(Map("port" -> "garbage")).left.getOrElse(Nil).head
    err.expectedValue shouldBe Some("Integer")
  }

  // =========================================================================
  // 14. Statelessness and reusability
  // =========================================================================

  "ConfigLoader instances" should "be stateless and reusable across calls" in {
    val loader = ConfigLoader.build(
      field[String]("name"),
      field[Int]("port")
    )(TwoFields.apply)

    loader.load(Map("name" -> "a", "port" -> "1")) shouldBe Right(TwoFields("a", 1))
    loader.load(Map("name" -> "b", "port" -> "2")) shouldBe Right(TwoFields("b", 2))
    loader.load(Map.empty).isLeft shouldBe true
    loader.load(Map("name" -> "c", "port" -> "3")) shouldBe Right(TwoFields("c", 3))
  }

  // =========================================================================
  // 15. Property name with dots and special patterns
  // =========================================================================

  "ConfigLoader property names" should "handle dotted property names" in {
    val loader = ConfigLoader.build(
      field[String]("server.http.host"),
      field[Int]("server.http.port")
    )((h, p) => (h, p))

    loader.load(Map("server.http.host" -> "0.0.0.0", "server.http.port" -> "443")) shouldBe
      Right(("0.0.0.0", 443))
  }

  it should "handle hyphenated property names" in {
    val loader = ConfigLoader.build(
      field[String]("my-app-name")
    )(identity)

    loader.load(Map("my-app-name" -> "svc")) shouldBe Right("svc")
  }

  it should "handle underscore property names" in {
    val loader = ConfigLoader.build(
      field[String]("my_app_name")
    )(identity)

    loader.load(Map("my_app_name" -> "svc")) shouldBe Right("svc")
  }

  // =========================================================================
  // 16. List fields — HOCON inline
  // =========================================================================

  "ConfigLoader listField HOCON inline" should "parse bracketed string list" in {
    val loader = ConfigLoader.build(
      listField[String]("tags")
    )(identity)

    loader.load(Map("tags" -> """["a", "b", "c"]""")) shouldBe Right(List("a", "b", "c"))
  }

  it should "parse bracketed int list" in {
    val loader = ConfigLoader.build(
      listField[Int]("sizes")
    )(identity)

    loader.load(Map("sizes" -> "[1, 2, 3]")) shouldBe Right(List(1, 2, 3))
  }

  it should "parse comma-separated values without brackets" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items" -> "a, b, c")) shouldBe Right(List("a", "b", "c"))
  }

  it should "parse comma-separated int values without brackets" in {
    val loader = ConfigLoader.build(
      listField[Int]("nums")
    )(identity)

    loader.load(Map("nums" -> "10, 20, 30")) shouldBe Right(List(10, 20, 30))
  }

  it should "parse empty list" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items" -> "[]")) shouldBe Right(List.empty[String])
  }

  it should "parse single-element list" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items" -> """["only"]""")) shouldBe Right(List("only"))
  }

  it should "handle HOCON newline-separated list" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    val hoconList = """["alpha", "beta", "gamma"]"""
    loader.load(Map("items" -> hoconList)) shouldBe Right(List("alpha", "beta", "gamma"))
  }

  // =========================================================================
  // 17. List fields — indexed keys
  // =========================================================================

  "ConfigLoader listField indexed keys" should "collect indexed keys in order" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items.0" -> "a", "items.1" -> "b", "items.2" -> "c")) shouldBe
      Right(List("a", "b", "c"))
  }

  it should "collect indexed int keys" in {
    val loader = ConfigLoader.build(
      listField[Int]("ports")
    )(identity)

    loader.load(Map("ports.0" -> "80", "ports.1" -> "443", "ports.2" -> "8080")) shouldBe
      Right(List(80, 443, 8080))
  }

  it should "sort by index even when keys are unordered" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items.2" -> "c", "items.0" -> "a", "items.1" -> "b")) shouldBe
      Right(List("a", "b", "c"))
  }

  it should "ignore non-integer suffixes" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map(
      "items.0" -> "a", "items.1" -> "b",
      "items.name" -> "ignored", "items.nested.key" -> "also-ignored"
    )) shouldBe Right(List("a", "b"))
  }

  // =========================================================================
  // 18. List fields — HOCON takes priority over indexed
  // =========================================================================

  "ConfigLoader listField priority" should "prefer HOCON inline over indexed keys" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map(
      "items" -> """["hocon-a", "hocon-b"]""",
      "items.0" -> "indexed-a", "items.1" -> "indexed-b"
    )) shouldBe Right(List("hocon-a", "hocon-b"))
  }

  // =========================================================================
  // 19. List fields — element validation
  // =========================================================================

  "ConfigLoader listField element validation" should "validate each element with rule" in {
    val loader = ConfigLoader.build(
      listField[String]("emails", Rules.email)
    )(identity)

    loader.load(Map("emails" -> """["good@example.com", "also-good@test.org"]""")).isRight shouldBe true
  }

  it should "fail with indexed error name when element violates rule" in {
    val loader = ConfigLoader.build(
      listField[String]("emails", Rules.email)
    )(identity)

    val result = loader.load(Map("emails" -> """["good@example.com", "bad-email", "another-bad"]"""))
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors should have size 2
    errors.map(_.propertyName).toSet shouldBe Set("emails[1]", "emails[2]")
  }

  it should "validate each element of indexed-key list" in {
    val loader = ConfigLoader.build(
      listField[Int]("ports", Rules.port)
    )(identity)

    val result = loader.load(Map("ports.0" -> "80", "ports.1" -> "99999"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "ports[1]"
  }

  it should "accumulate element errors with other field errors" in {
    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank),
      listField[Int]("ports", Rules.port)
    )((n, p) => (n, p))

    val result = loader.load(Map("name" -> "", "ports" -> "[99999, 0]"))
    result.isLeft shouldBe true
    val errors = result.left.getOrElse(Nil)
    errors.size should be >= 3
    errors.map(_.propertyName).toSet should contain allOf("name", "ports[0]", "ports[1]")
  }

  // =========================================================================
  // 20. List fields — element conversion errors
  // =========================================================================

  "ConfigLoader listField conversion errors" should "produce indexed error for bad element" in {
    val loader = ConfigLoader.build(
      listField[Int]("nums")
    )(identity)

    val result = loader.load(Map("nums" -> "[1, abc, 3]"))
    result.isLeft shouldBe true
    val err = result.left.getOrElse(Nil).head
    err.propertyName shouldBe "nums[1]"
    err.message should include("Integer")
    err.actualValue shouldBe Some("abc")
  }

  it should "accumulate multiple element conversion errors" in {
    val loader = ConfigLoader.build(
      listField[Int]("nums")
    )(identity)

    val result = loader.load(Map("nums" -> "[bad, worse, 3]"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil) should have size 2
  }

  // =========================================================================
  // 21. List fields — defaults and missing
  // =========================================================================

  "ConfigLoader listField defaults" should "use default when missing" in {
    val loader = ConfigLoader.build(
      listField[Int]("sizes").withDefault(List(128, 256))
    )(identity)

    loader.load(Map.empty) shouldBe Right(List(128, 256))
  }

  it should "prefer provided value over default" in {
    val loader = ConfigLoader.build(
      listField[Int]("sizes").withDefault(List(128, 256))
    )(identity)

    loader.load(Map("sizes" -> "[1, 2, 3]")) shouldBe Right(List(1, 2, 3))
  }

  it should "fail when missing without default" in {
    val loader = ConfigLoader.build(
      listField[String]("tags")
    )(identity)

    val result = loader.load(Map.empty)
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.message should include("missing")
  }

  it should "treat empty string as missing" in {
    val loader = ConfigLoader.build(
      listField[String]("tags").withDefault(List("fallback"))
    )(identity)

    loader.load(Map("tags" -> "")) shouldBe Right(List("fallback"))
  }

  // =========================================================================
  // 22. List fields — integration with other field types
  // =========================================================================

  "ConfigLoader listField integration" should "work in case class with other field types" in {
    case class ServiceConfig(name: String, port: Int, tags: List[String], desc: Option[String])

    val loader = ConfigLoader.build(
      field[String]("name", Rules.notBlank),
      field[Int]("port", Rules.port).withDefault(8080),
      listField[String]("tags", Rules.notBlank),
      optional[String]("desc")
    )(ServiceConfig.apply)

    val result = loader.load(Map(
      "name" -> "my-svc",
      "tags" -> """["api", "internal"]"""
    ))
    result shouldBe Right(ServiceConfig("my-svc", 8080, List("api", "internal"), None))
  }

  it should "work inside nested case class" in {
    case class Inner(items: List[String], count: Int)
    case class Outer(name: String, inner: Inner)

    implicit val innerLoader: ConfigLoader[Inner] = ConfigLoader.build(
      listField[String]("items"),
      field[Int]("count")
    )(Inner.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[Inner]("sub.")
    )(Outer.apply)

    loader.load(Map(
      "name" -> "outer",
      "sub.items" -> """["x", "y"]""",
      "sub.count" -> "2"
    )) shouldBe Right(Outer("outer", Inner(List("x", "y"), 2)))
  }

  it should "work with indexed keys inside nested case class" in {
    case class Inner(items: List[Int])
    case class Outer(name: String, inner: Inner)

    implicit val innerLoader: ConfigLoader[Inner] = ConfigLoader.build(
      listField[Int]("items")
    )(Inner.apply)

    val loader = ConfigLoader.build(
      field[String]("name"),
      nested[Inner]("sub.")
    )(Outer.apply)

    loader.load(Map(
      "name" -> "outer",
      "sub.items.0" -> "10",
      "sub.items.1" -> "20"
    )) shouldBe Right(Outer("outer", Inner(List(10, 20))))
  }

  // =========================================================================
  // 23. List fields — non-primitive element types
  // =========================================================================

  "ConfigLoader listField non-primitives" should "support URL elements" in {
    import java.net.URL
    val loader = ConfigLoader.build(
      listField[URL]("endpoints")
    )(identity)

    val result = loader.load(Map("endpoints" -> """["https://a.com", "https://b.com"]"""))
    result.isRight shouldBe true
    result.toOption.get.map(_.toString) shouldBe List("https://a.com", "https://b.com")
  }

  it should "support Duration elements" in {
    import java.time.Duration
    val loader = ConfigLoader.build(
      listField[Duration]("timeouts")
    )(identity)

    val result = loader.load(Map("timeouts" -> """["PT10S", "PT30S", "PT1M"]"""))
    result.isRight shouldBe true
    result.toOption.get shouldBe List(Duration.ofSeconds(10), Duration.ofSeconds(30), Duration.ofMinutes(1))
  }

  // =========================================================================
  // 24. List fields — edge cases and parse errors
  // =========================================================================

  "ConfigLoader listField edge cases" should "parse single value without brackets or commas" in {
    val loader = ConfigLoader.build(
      listField[String]("item")
    )(identity)

    loader.load(Map("item" -> "singlevalue")) shouldBe Right(List("singlevalue"))
  }

  it should "produce parse error for malformed HOCON with brackets" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    val result = loader.load(Map("items" -> "[unclosed"))
    result.isLeft shouldBe true
    result.left.getOrElse(Nil).head.propertyName shouldBe "items"
    result.left.getOrElse(Nil).head.message should include("parse")
  }

  it should "handle quoted HOCON strings with special characters" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items" -> """["a=b", "c:d", "e#f"]""")) shouldBe
      Right(List("a=b", "c:d", "e#f"))
  }

  it should "allow empty list even with element rule (no elements to validate)" in {
    val loader = ConfigLoader.build(
      listField[String]("items", Rules.notBlank)
    )(identity)

    loader.load(Map("items" -> "[]")) shouldBe Right(List.empty[String])
  }

  it should "handle sparse indexed keys by collecting only present indices" in {
    val loader = ConfigLoader.build(
      listField[String]("items")
    )(identity)

    loader.load(Map("items.0" -> "a", "items.5" -> "b")) shouldBe Right(List("a", "b"))
  }
}
