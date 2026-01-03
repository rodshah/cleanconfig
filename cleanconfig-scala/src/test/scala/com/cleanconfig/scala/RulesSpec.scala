package com.cleanconfig.scala

import com.cleanconfig.scala.RuleOps._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RulesSpec extends AnyFlatSpec with Matchers {

  "Rules.notBlank" should "validate non-empty strings" in {
    val rule = Rules.notBlank
    val context = PropertyContext(Map.empty[String, String])

    val result1 = rule.validate("test.prop", "hello", context.underlying)
    result1.isValid shouldBe true

    val result2 = rule.validate("test.prop", "", context.underlying)
    result2.isValid shouldBe false

    val result3 = rule.validate("test.prop", "   ", context.underlying)
    result3.isValid shouldBe false
  }

  "Rules.minLength and maxLength" should "validate string length" in {
    val rule = Rules.minLength(3)
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("test", "abc", context.underlying).isValid shouldBe true
    rule.validate("test", "ab", context.underlying).isValid shouldBe false

    val maxRule = Rules.maxLength(5)
    maxRule.validate("test", "12345", context.underlying).isValid shouldBe true
    maxRule.validate("test", "123456", context.underlying).isValid shouldBe false
  }

  "Rules.email" should "validate email addresses" in {
    val rule = Rules.email
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("email", "test@example.com", context.underlying).isValid shouldBe true
    rule.validate("email", "invalid-email", context.underlying).isValid shouldBe false
  }

  "Rules.pattern" should "validate regex patterns" in {
    val rule = Rules.pattern("^[0-9]+$")
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("test", "12345", context.underlying).isValid shouldBe true
    rule.validate("test", "abc", context.underlying).isValid shouldBe false
  }

  "Rules.positive" should "validate positive numbers" in {
    val rule = Rules.positive
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("num", Integer.valueOf(10), context.underlying).isValid shouldBe true
    rule.validate("num", Integer.valueOf(0), context.underlying).isValid shouldBe false
    rule.validate("num", Integer.valueOf(-5), context.underlying).isValid shouldBe false
  }

  "Rules.integerBetween" should "validate integer ranges" in {
    val rule = Rules.integerBetween(1, 10)
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("num", Integer.valueOf(5), context.underlying).isValid shouldBe true
    rule.validate("num", Integer.valueOf(1), context.underlying).isValid shouldBe true
    rule.validate("num", Integer.valueOf(10), context.underlying).isValid shouldBe true
    rule.validate("num", Integer.valueOf(0), context.underlying).isValid shouldBe false
    rule.validate("num", Integer.valueOf(11), context.underlying).isValid shouldBe false
  }

  "Rules.port" should "validate port numbers" in {
    val rule = Rules.port
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("port", Integer.valueOf(8080), context.underlying).isValid shouldBe true
    rule.validate("port", Integer.valueOf(1), context.underlying).isValid shouldBe true
    rule.validate("port", Integer.valueOf(65535), context.underlying).isValid shouldBe true
    rule.validate("port", Integer.valueOf(0), context.underlying).isValid shouldBe false
    rule.validate("port", Integer.valueOf(99999), context.underlying).isValid shouldBe false
  }

  "ValidationRuleOps.&&" should "compose rules with AND" in {
    val rule = Rules.notBlank && Rules.minLength(3) && Rules.maxLength(10)
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("test", "hello", context.underlying).isValid shouldBe true
    rule.validate("test", "", context.underlying).isValid shouldBe false
    rule.validate("test", "ab", context.underlying).isValid shouldBe false
    rule.validate("test", "verylongstring", context.underlying).isValid shouldBe false
  }

  "ValidationRuleOps.||" should "compose rules with OR" in {
    val rule = Rules.startsWith("http://") || Rules.startsWith("https://")
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("url", "http://example.com", context.underlying).isValid shouldBe true
    rule.validate("url", "https://example.com", context.underlying).isValid shouldBe true
    rule.validate("url", "ftp://example.com", context.underlying).isValid shouldBe false
  }

  "Rules.custom" should "allow custom validation logic" in {
    val rule = Rules.custom[Int]("Must be even")(n => n % 2 == 0)
    val context = PropertyContext(Map.empty[String, String])

    rule.validate("num", 4, context.underlying).isValid shouldBe true
    rule.validate("num", 5, context.underlying).isValid shouldBe false
  }

  "Complex composition" should "work with multiple operators" in {
    val emailRule = Rules.notBlank && Rules.email && Rules.endsWith("@company.com")
    val context = PropertyContext(Map.empty[String, String])

    emailRule.validate("email", "user@company.com", context.underlying).isValid shouldBe true
    emailRule.validate("email", "user@other.com", context.underlying).isValid shouldBe false
    emailRule.validate("email", "", context.underlying).isValid shouldBe false
  }
}
