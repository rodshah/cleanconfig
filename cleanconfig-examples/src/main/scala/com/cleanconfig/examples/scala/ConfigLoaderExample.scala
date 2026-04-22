package com.cleanconfig.examples.scala

import com.cleanconfig.scala._
import com.cleanconfig.scala.ConfigLoader._
import com.cleanconfig.scala.RuleOps._

/**
 * Demonstrates ConfigLoader — loading validated configuration directly into
 * Scala case classes from a Map[String, String].
 *
 * Shows:
 * - Basic case class binding with type conversion
 * - Validation rules on fields (single, composed, cross-type)
 * - Default values
 * - Optional fields
 * - Nested case classes with key prefix stripping
 * - Deeply nested (3 levels)
 * - Multiple nested in one loader
 * - Error accumulation across all fields
 * - Non-primitive types (URL, Duration)
 * - Summoner syntax
 */
object ConfigLoaderExample extends App {

  // ── Case classes ──────────────────────────────────────────────────────────

  case class DatabaseConfig(url: String, maxPool: Int, timeout: Long)
  case class CacheConfig(enabled: Boolean, ttlSeconds: Int, maxEntries: Int)
  case class ServerConfig(
      name: String,
      port: Int,
      debug: Boolean,
      db: DatabaseConfig,
      cache: CacheConfig,
      description: Option[String],
      adminEmail: Option[String]
  )
  case class SimpleConfig(host: String, port: Int)
  case class EndpointConfig(baseUrl: java.net.URL, timeout: java.time.Duration)

  // ── ConfigLoader definitions ──────────────────────────────────────────────

  implicit val dbLoader: ConfigLoader[DatabaseConfig] = ConfigLoader.build(
    field[String]("url", Rules.notBlank && Rules.startsWith("jdbc:")),
    field[Int]("max.pool", Rules.integerBetween(1, 100)).withDefault(10),
    field[Long]("timeout.ms").withDefault(5000L)
  )(DatabaseConfig.apply)

  implicit val cacheLoader: ConfigLoader[CacheConfig] = ConfigLoader.build(
    field[Boolean]("enabled").withDefault(false),
    field[Int]("ttl.seconds", Rules.integerBetween(1, 86400)).withDefault(300),
    field[Int]("max.entries", Rules.integerBetween(1, 1000000)).withDefault(10000)
  )(CacheConfig.apply)

  implicit val serverLoader: ConfigLoader[ServerConfig] = ConfigLoader.build(
    field[String]("app.name", Rules.notBlank && Rules.maxLength(50)),
    field[Int]("app.port", Rules.port).withDefault(8080),
    field[Boolean]("app.debug").withDefault(false),
    nested[DatabaseConfig]("db."),
    nested[CacheConfig]("cache."),
    optional[String]("app.description", Rules.maxLength(200)),
    optional[String]("app.admin.email", Rules.email)
  )(ServerConfig.apply)

  def printSeparator(title: String): Unit = {
    println()
    println("=" * 65)
    println(s"  $title")
    println("=" * 65)
  }

  def printErrors(errors: List[ValidationError]): Unit = {
    println(s"  ${errors.size} error(s):")
    errors.foreach { e =>
      val actual = e.actualValue.map(v => s" (was: '$v')").getOrElse("")
      println(s"    - [${e.propertyName}] ${e.message}$actual")
    }
  }

  // ── Example 1: Basic case class ──────────────────────────────────────────

  printSeparator("1. Basic case class — two fields, no rules")

  val basicLoader = ConfigLoader.build(
    field[String]("host"),
    field[Int]("port")
  )(SimpleConfig.apply)

  basicLoader.load(Map("host" -> "localhost", "port" -> "9090")) match {
    case Right(cfg) => println(s"  Loaded: SimpleConfig(host=${cfg.host}, port=${cfg.port})")
    case Left(err)  => printErrors(err)
  }

  // ── Example 2: Validation rules ──────────────────────────────────────────

  printSeparator("2. Validation rules — single, composed, cross-type")

  val rulesLoader = ConfigLoader.build(
    field[String]("name", Rules.notBlank && Rules.minLength(3) && Rules.maxLength(20)),
    field[Int]("port", Rules.port),
    field[String]("env", Rules.oneOf("dev", "staging", "prod")),
    field[String]("url", Rules.startsWith("http://") || Rules.startsWith("https://"))
  )((name, port, env, url) => (name, port, env, url))

  println("  Valid input:")
  rulesLoader.load(Map(
    "name" -> "my-service", "port" -> "443", "env" -> "prod", "url" -> "https://api.example.com"
  )) match {
    case Right(cfg) => println(s"    Result: $cfg")
    case Left(err)  => printErrors(err)
  }

  println()
  println("  Invalid input (all fields fail):")
  rulesLoader.load(Map(
    "name" -> "ab", "port" -> "99999", "env" -> "qa", "url" -> "ftp://bad"
  )) match {
    case Right(_)  => println("    Unexpected success")
    case Left(err) => printErrors(err)
  }

  // ── Example 3: Default values ────────────────────────────────────────────

  printSeparator("3. Default values — minimal input, defaults fill the rest")

  val defaultsLoader = ConfigLoader.build(
    field[String]("app.name", Rules.notBlank),
    field[Int]("app.port").withDefault(8080),
    field[Boolean]("app.debug").withDefault(false),
    field[Int]("app.workers").withDefault(4),
    field[String]("app.log.level").withDefault("INFO")
  )((name, port, debug, workers, logLevel) => (name, port, debug, workers, logLevel))

  defaultsLoader.load(Map("app.name" -> "minimal-svc")) match {
    case Right((name, port, debug, workers, logLevel)) =>
      println(s"  name=$name, port=$port (default), debug=$debug (default), " +
        s"workers=$workers (default), logLevel=$logLevel (default)")
    case Left(err) => printErrors(err)
  }

  // ── Example 4: Optional fields ───────────────────────────────────────────

  printSeparator("4. Optional fields — missing yields None, present yields Some")

  case class ProfileConfig(
      username: String,
      bio: Option[String],
      website: Option[String],
      age: Option[Int]
  )

  val profileLoader = ConfigLoader.build(
    field[String]("username", Rules.notBlank),
    optional[String]("bio", Rules.maxLength(500)),
    optional[String]("website", Rules.url),
    optional[Int]("age", Rules.integerBetween(1, 150))
  )(ProfileConfig.apply)

  println("  All optional fields present:")
  profileLoader.load(Map(
    "username" -> "jdoe",
    "bio" -> "Engineer at Acme",
    "website" -> "https://jdoe.dev",
    "age" -> "30"
  )) match {
    case Right(p) => println(s"    $p")
    case Left(e)  => printErrors(e)
  }

  println()
  println("  Only required field provided:")
  profileLoader.load(Map("username" -> "jdoe")) match {
    case Right(p) => println(s"    $p")
    case Left(e)  => printErrors(e)
  }

  println()
  println("  Optional field present but invalid:")
  profileLoader.load(Map("username" -> "jdoe", "website" -> "not-a-url")) match {
    case Right(_) => println("    Unexpected success")
    case Left(e)  => printErrors(e)
  }

  // ── Example 5: Nested case classes ───────────────────────────────────────

  printSeparator("5. Nested case classes — prefix stripping, defaults in nested")

  serverLoader.load(Map(
    "app.name" -> "order-service",
    "app.port" -> "9090",
    "db.url" -> "jdbc:postgresql://localhost:5432/orders",
    "db.max.pool" -> "25",
    "cache.enabled" -> "true",
    "cache.ttl.seconds" -> "600",
    "app.description" -> "Handles order processing",
    "app.admin.email" -> "admin@example.com"
  )) match {
    case Right(cfg) =>
      println(s"  Server:      ${cfg.name}:${cfg.port} (debug=${cfg.debug})")
      println(s"  DB:          ${cfg.db.url} (pool=${cfg.db.maxPool}, timeout=${cfg.db.timeout}ms)")
      println(s"  Cache:       enabled=${cfg.cache.enabled}, ttl=${cfg.cache.ttlSeconds}s, max=${cfg.cache.maxEntries}")
      println(s"  Description: ${cfg.description.getOrElse("(none)")}")
      println(s"  Admin:       ${cfg.adminEmail.getOrElse("(none)")}")
    case Left(err) => printErrors(err)
  }

  // ── Example 6: Minimal nested (all defaults) ────────────────────────────

  printSeparator("6. Minimal nested — only truly required fields provided")

  serverLoader.load(Map(
    "app.name" -> "bare-minimum",
    "db.url" -> "jdbc:h2:mem:test"
  )) match {
    case Right(cfg) =>
      println(s"  Server:      ${cfg.name}:${cfg.port}")
      println(s"  DB:          ${cfg.db.url} (pool=${cfg.db.maxPool}, timeout=${cfg.db.timeout}ms)")
      println(s"  Cache:       enabled=${cfg.cache.enabled}, ttl=${cfg.cache.ttlSeconds}s")
      println(s"  Description: ${cfg.description.getOrElse("(none)")}")
      println(s"  Admin:       ${cfg.adminEmail.getOrElse("(none)")}")
    case Left(err) => printErrors(err)
  }

  // ── Example 7: Deep nesting (3 levels) ──────────────────────────────────

  printSeparator("7. Deep nesting — 3-level case class hierarchy")

  case class ClusterConfig(app: ServerConfig, region: String)

  implicit val clusterLoader: ConfigLoader[ClusterConfig] = ConfigLoader.build(
    nested[ServerConfig]("primary."),
    field[String]("cluster.region", Rules.oneOf("us-east-1", "us-west-2", "eu-west-1"))
  )(ClusterConfig.apply)

  clusterLoader.load(Map(
    "primary.app.name" -> "cluster-svc",
    "primary.db.url" -> "jdbc:pg://db-primary:5432/app",
    "cluster.region" -> "us-east-1"
  )) match {
    case Right(cluster) =>
      println(s"  Region: ${cluster.region}")
      println(s"  App:    ${cluster.app.name}:${cluster.app.port}")
      println(s"  DB:     ${cluster.app.db.url}")
    case Left(err) => printErrors(err)
  }

  // ── Example 8: Error accumulation across all levels ─────────────────────

  printSeparator("8. Error accumulation — failures across parent, nested, and optional")

  serverLoader.load(Map(
    "app.name" -> "",                    // fails: notBlank
    "app.port" -> "99999",               // fails: port range
    "db.url" -> "not-a-jdbc-url",        // fails: startsWith("jdbc:")
    "db.max.pool" -> "999",              // fails: integerBetween(1, 100)
    "cache.ttl.seconds" -> "-1",         // fails: integerBetween(1, 86400)
    "app.admin.email" -> "not-email"     // fails: email
  )) match {
    case Right(_)  => println("  Unexpected success")
    case Left(err) => printErrors(err)
  }

  // ── Example 9: Non-primitive types (URL, Duration) ──────────────────────

  printSeparator("9. Non-primitive types — URL and Duration fields")

  val endpointLoader = ConfigLoader.build(
    field[java.net.URL]("base.url"),
    field[java.time.Duration]("timeout")
  )(EndpointConfig.apply)

  println("  Valid:")
  endpointLoader.load(Map(
    "base.url" -> "https://api.example.com/v2",
    "timeout" -> "PT30S"
  )) match {
    case Right(ep) => println(s"    URL=${ep.baseUrl}, timeout=${ep.timeout}")
    case Left(err) => printErrors(err)
  }

  println()
  println("  Invalid:")
  endpointLoader.load(Map(
    "base.url" -> "not a url",
    "timeout" -> "30 seconds"
  )) match {
    case Right(_)  => println("    Unexpected success")
    case Left(err) => printErrors(err)
  }

  // ── Example 10: List fields — HOCON inline ───────────────────────────────

  printSeparator("10. List fields — HOCON inline and comma-separated")

  case class PipelineConfig(name: String, topics: List[String], partitionSizes: List[Int])

  val pipelineLoader = ConfigLoader.build(
    field[String]("name", Rules.notBlank),
    listField[String]("topics", Rules.notBlank),
    listField[Int]("partition.sizes", Rules.integerBetween(1, 1024)).withDefault(List(128, 256))
  )(PipelineConfig.apply)

  println("  HOCON bracketed syntax:")
  pipelineLoader.load(Map(
    "name" -> "event-archival",
    "topics" -> """["orders", "payments", "users"]"""
  )) match {
    case Right(cfg) =>
      println(s"    topics: ${cfg.topics}")
      println(s"    partitions: ${cfg.partitionSizes} (default)")
    case Left(err) => printErrors(err)
  }

  println()
  println("  Comma-separated syntax:")
  pipelineLoader.load(Map(
    "name" -> "analytics",
    "topics" -> "clicks, impressions, conversions",
    "partition.sizes" -> "64, 128, 256, 512"
  )) match {
    case Right(cfg) =>
      println(s"    topics: ${cfg.topics}")
      println(s"    partitions: ${cfg.partitionSizes}")
    case Left(err) => printErrors(err)
  }

  // ── Example 11: List fields — indexed keys (from HoconPropertySource) ───

  printSeparator("11. List fields — indexed keys (HoconPropertySource format)")

  pipelineLoader.load(Map(
    "name" -> "ingestion",
    "topics.0" -> "raw-events",
    "topics.1" -> "enriched-events",
    "partition.sizes.0" -> "256",
    "partition.sizes.1" -> "512"
  )) match {
    case Right(cfg) =>
      println(s"    topics: ${cfg.topics}")
      println(s"    partitions: ${cfg.partitionSizes}")
    case Left(err) => printErrors(err)
  }

  // ── Example 12: List fields — element validation errors ─────────────────

  printSeparator("12. List fields — element validation errors (indexed)")

  pipelineLoader.load(Map(
    "name" -> "bad-pipeline",
    "topics" -> """["", "valid-topic", ""]""",
    "partition.sizes" -> "[0, 128, 9999]"
  )) match {
    case Right(_)  => println("  Unexpected success")
    case Left(err) => printErrors(err)
  }

  // ── Example 13: Summoner syntax ─────────────────────────────────────────

  printSeparator("13. Summoner syntax — ConfigLoader[ServerConfig].load(...)")

  ConfigLoader[ServerConfig].load(Map(
    "app.name" -> "summoner-app",
    "db.url" -> "jdbc:mysql://db:3306/app"
  )) match {
    case Right(cfg) => println(s"  Loaded via summoner: ${cfg.name}:${cfg.port}")
    case Left(err)  => printErrors(err)
  }

  // ── Example 14: Pattern matching on Either ──────────────────────────────

  printSeparator("14. Idiomatic Scala — pattern matching, map, fold")

  val result = serverLoader.load(Map(
    "app.name" -> "fold-demo",
    "db.url" -> "jdbc:h2:mem:fold"
  ))

  // fold
  val message = result.fold(
    errors => s"Failed with ${errors.size} error(s)",
    config => s"Loaded ${config.name} on port ${config.port}"
  )
  println(s"  fold:     $message")

  // map
  val portOpt = result.map(_.port).toOption
  println(s"  map+toOpt: port=$portOpt")

  // getOrElse
  val fallback = result.getOrElse(ServerConfig(
    "fallback", 80, false,
    DatabaseConfig("jdbc:h2:mem:fallback", 5, 1000),
    CacheConfig(false, 60, 100),
    None, None
  ))
  println(s"  getOrElse: ${fallback.name}")

  println()
}
