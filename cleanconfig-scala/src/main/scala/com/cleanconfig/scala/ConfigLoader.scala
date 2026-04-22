package com.cleanconfig.scala

import com.cleanconfig.core.converter.TypeConverterRegistry
import com.cleanconfig.core.impl.DefaultPropertyContext
import com.cleanconfig.core.validation.{
  ValidationRule,
  ValidationError => JavaValidationError
}
import com.typesafe.config.ConfigFactory
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * Loads a `Map[String, String]` into a typed case class `A` with full
 * error accumulation, type conversion, and validation.
 *
 * {{{
 * import com.cleanconfig.scala.ConfigLoader._
 *
 * case class ServerConfig(host: String, port: Int)
 *
 * implicit val loader: ConfigLoader[ServerConfig] = ConfigLoader.build(
 *   field[String]("server.host", Rules.notBlank),
 *   field[Int]("server.port", Rules.port).withDefault(8080)
 * )(ServerConfig.apply)
 *
 * loader.load(Map("server.host" -> "localhost"))
 * // Right(ServerConfig("localhost", 8080))
 * }}}
 *
 * @tparam A the target case class type
 */
trait ConfigLoader[A] {

  /**
   * Loads and validates properties into an instance of `A`.
   *
   * All fields are validated independently (applicative, not monadic).
   * If any field fails, all errors are accumulated and returned.
   *
   * @param properties raw configuration map
   * @return either accumulated validation errors or a valid instance
   */
  def load(properties: Map[String, String]): Either[List[ValidationError], A]
}

object ConfigLoader {

  /** Summons an implicit ConfigLoader instance. */
  def apply[A](implicit loader: ConfigLoader[A]): ConfigLoader[A] = loader

  // ---------------------------------------------------------------------------
  // DSL entry points
  // ---------------------------------------------------------------------------

  /** Creates a required field definition. Fails if the key is absent and no default is set. */
  def field[T: FieldType](name: String): RequiredFieldDef[T] =
    RequiredFieldDef(name, FieldType[T])

  /** Creates a required field definition with a validation rule. */
  def field[T: FieldType](name: String, rule: ValidationRule[_]): RequiredFieldDef[T] =
    RequiredFieldDef(name, FieldType[T], rule = Some(rule))

  /** Creates an optional field. Missing key yields `None`, present key yields `Some(value)`. */
  def optional[T: FieldType](name: String): OptionalFieldDef[T] =
    OptionalFieldDef(name, FieldType[T])

  /** Creates an optional field with a validation rule (applied only when the key is present). */
  def optional[T: FieldType](name: String, rule: ValidationRule[_]): OptionalFieldDef[T] =
    OptionalFieldDef(name, FieldType[T], rule = Some(rule))

  /** Creates a nested field that delegates to another `ConfigLoader` with key prefix stripping. */
  def nested[T](prefix: String = "")(implicit loader: ConfigLoader[T]): NestedFieldDef[T] =
    NestedFieldDef(prefix, loader)

  /** Creates a list field. Parses HOCON inline syntax (`[a, b, c]`) or indexed keys (`key.0`, `key.1`). */
  def listField[T: FieldType](name: String): ListFieldDef[T] =
    ListFieldDef(name, FieldType[T])

  /** Creates a list field with an element-level validation rule. */
  def listField[T: FieldType](name: String, elementRule: ValidationRule[_]): ListFieldDef[T] =
    ListFieldDef(name, FieldType[T], elementRule = Some(elementRule))

  // ---------------------------------------------------------------------------
  // Internal implementation
  // ---------------------------------------------------------------------------

  private[scala] class ConfigLoaderImpl[A](
      fields: Seq[FieldDef[_]],
      construct: Seq[Any] => A
  ) extends ConfigLoader[A] {

    override def load(properties: Map[String, String]): Either[List[ValidationError], A] = {
      val registry = TypeConverterRegistry.getInstance()
      val javaContext = new DefaultPropertyContext(properties.asJava, registry, java.util.Collections.emptyMap())

      val results: Seq[Either[List[JavaValidationError], Any]] = fields.map {
        case f: RequiredFieldDef[_] => loadRequired(f, properties, registry, javaContext)
        case f: OptionalFieldDef[_] => loadOptional(f, properties, registry, javaContext)
        case f: NestedFieldDef[_]   => loadNested(f, properties)
        case f: ListFieldDef[_]     => loadList(f, properties, registry, javaContext)
      }

      val errors = results.collect { case Left(errs) => errs }.flatten.toList
      if (errors.nonEmpty) {
        Left(errors.map(ValidationError(_)))
      } else {
        val values = results.map {
          case Right(v) => v
          case Left(_)  => throw new IllegalStateException("Unreachable: errors already checked")
        }
        Right(construct(values))
      }
    }

    private def loadRequired(
        f: RequiredFieldDef[_],
        props: Map[String, String],
        registry: TypeConverterRegistry,
        ctx: DefaultPropertyContext
    ): Either[List[JavaValidationError], Any] = {
      props.get(f.name) match {
        case None | Some("") =>
          f.default match {
            case Some(d) => Right(d)
            case None    => Left(List(missingError(f.name)))
          }
        case Some(raw) =>
          convertAndValidate(f.name, raw, f.fieldType.javaClass, f.rule, registry, ctx)
      }
    }

    private def loadOptional(
        f: OptionalFieldDef[_],
        props: Map[String, String],
        registry: TypeConverterRegistry,
        ctx: DefaultPropertyContext
    ): Either[List[JavaValidationError], Any] = {
      props.get(f.name) match {
        case None | Some("") => Right(None)
        case Some(raw) =>
          convertAndValidate(f.name, raw, f.fieldType.javaClass, f.rule, registry, ctx)
            .map(Some(_))
      }
    }

    private def loadNested(
        f: NestedFieldDef[_],
        props: Map[String, String]
    ): Either[List[JavaValidationError], Any] = {
      val stripped = if (f.prefix.isEmpty) {
        props
      } else {
        props.collect {
          case (k, v) if k.startsWith(f.prefix) => (k.stripPrefix(f.prefix), v)
        }
      }

      f.loader.load(stripped) match {
        case Right(value) => Right(value)
        case Left(errors) =>
          val prefixed = if (f.prefix.isEmpty) {
            errors.map(_.underlying)
          } else {
            errors.map { err =>
              val builder = JavaValidationError.builder()
                .propertyName(f.prefix + err.propertyName)
                .errorMessage(err.message)
                .actualValue(err.actualValue.orNull)
                .expectedValue(err.expectedValue.orNull)
              err.suggestion.foreach(builder.suggestion)
              Option(err.underlying.getErrorCode).foreach(builder.errorCode)
              builder.build()
            }
          }
          Left(prefixed)
      }
    }

    private def convertAndValidate(
        name: String,
        raw: String,
        targetClass: Class[_],
        rule: Option[ValidationRule[_]],
        registry: TypeConverterRegistry,
        ctx: DefaultPropertyContext
    ): Either[List[JavaValidationError], Any] = {
      val converted = registry.convert(raw, targetClass)
      if (!converted.isPresent) {
        Left(List(conversionError(name, raw, targetClass)))
      } else {
        val value = converted.get()
        rule match {
          case None => Right(value)
          case Some(r) =>
            val result = r.asInstanceOf[ValidationRule[Any]].validate(name, value, ctx)
            if (result.isValid) Right(value)
            else Left(result.getErrors.asScala.toList)
        }
      }
    }

    private def missingError(name: String): JavaValidationError =
      JavaValidationError.builder()
        .propertyName(name)
        .errorMessage(s"Required property '$name' is missing")
        .build()

    private def conversionError(name: String, raw: String, targetClass: Class[_]): JavaValidationError =
      JavaValidationError.builder()
        .propertyName(name)
        .errorMessage(s"Cannot convert value '$raw' to ${targetClass.getSimpleName} for property '$name'")
        .actualValue(raw)
        .expectedValue(targetClass.getSimpleName)
        .build()

    private def listParseError(name: String, raw: String, detail: String): JavaValidationError =
      JavaValidationError.builder()
        .propertyName(name)
        .errorMessage(s"Cannot parse list value for property '$name': $detail")
        .actualValue(raw)
        .build()

    private def loadList(
        f: ListFieldDef[_],
        props: Map[String, String],
        registry: TypeConverterRegistry,
        ctx: DefaultPropertyContext
    ): Either[List[JavaValidationError], Any] = {
      extractListStrings(f.name, props) match {
        case Right(None) =>
          f.default match {
            case Some(d) => Right(d)
            case None    => Left(List(missingError(f.name)))
          }
        case Right(Some(elements)) =>
          convertAndValidateElements(f.name, elements, f.fieldType.javaClass, f.elementRule, registry, ctx)
        case Left(err) =>
          Left(List(err))
      }
    }

    /**
     * Extracts raw string elements from HOCON inline value or indexed keys.
     * Returns Right(None) if missing, Right(Some(list)) if found, Left(error) if parse fails.
     */
    private def extractListStrings(name: String, props: Map[String, String]): Either[JavaValidationError, Option[List[String]]] = {
      props.get(name) match {
        case Some(raw) if raw.trim.isEmpty => Right(None)
        case Some(raw) =>
          val toParse = if (raw.trim.startsWith("[")) raw else s"[$raw]"
          Try {
            ConfigFactory.parseString(s"""_list = $toParse""")
              .getStringList("_list")
              .asScala.toList
          }.fold(
            ex => Left(listParseError(name, raw, ex.getMessage)),
            list => Right(Some(list))
          )
        case None =>
          val prefix = name + "."
          val indexed = props.collect {
            case (k, v) if k.startsWith(prefix) =>
              val suffix = k.stripPrefix(prefix)
              Try(suffix.toInt).toOption.map(i => (i, v))
          }.flatten.toSeq.sortBy(_._1).map(_._2).toList

          Right(if (indexed.nonEmpty) Some(indexed) else None)
      }
    }

    /** Converts and validates each element, accumulating errors with indexed property names. */
    private def convertAndValidateElements(
        name: String,
        elements: List[String],
        targetClass: Class[_],
        rule: Option[ValidationRule[_]],
        registry: TypeConverterRegistry,
        ctx: DefaultPropertyContext
    ): Either[List[JavaValidationError], Any] = {
      val results: List[Either[List[JavaValidationError], Any]] = elements.zipWithIndex.map { case (raw, idx) =>
        val elemName = s"$name[$idx]"
        val converted = registry.convert(raw, targetClass)
        if (!converted.isPresent) {
          Left(List(conversionError(elemName, raw, targetClass)))
        } else {
          val value = converted.get()
          rule match {
            case None => Right(value)
            case Some(r) =>
              val result = r.asInstanceOf[ValidationRule[Any]].validate(elemName, value, ctx)
              if (result.isValid) Right(value)
              else Left(result.getErrors.asScala.toList)
          }
        }
      }

      val errors = results.collect { case Left(errs) => errs }.flatten
      if (errors.nonEmpty) Left(errors)
      else Right(results.map { case Right(v) => v; case _ => throw new IllegalStateException("Unreachable") })
    }
  }

  // ---------------------------------------------------------------------------
  // Arity-overloaded build methods (1–22)
  // ---------------------------------------------------------------------------

  def build[A, T1](f1: FieldDef[T1])(construct: T1 => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1), vs => construct(vs(0).asInstanceOf[T1]))

  def build[A, T1, T2](f1: FieldDef[T1], f2: FieldDef[T2])(construct: (T1, T2) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2]))

  def build[A, T1, T2, T3](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3])(construct: (T1, T2, T3) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3]))

  def build[A, T1, T2, T3, T4](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4])(construct: (T1, T2, T3, T4) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4]))

  def build[A, T1, T2, T3, T4, T5](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5])(construct: (T1, T2, T3, T4, T5) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5]))

  def build[A, T1, T2, T3, T4, T5, T6](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6])(construct: (T1, T2, T3, T4, T5, T6) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6]))

  def build[A, T1, T2, T3, T4, T5, T6, T7](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7])(construct: (T1, T2, T3, T4, T5, T6, T7) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8])(construct: (T1, T2, T3, T4, T5, T6, T7, T8) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17], f18: FieldDef[T18])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17], vs(17).asInstanceOf[T18]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17], f18: FieldDef[T18], f19: FieldDef[T19])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17], vs(17).asInstanceOf[T18], vs(18).asInstanceOf[T19]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17], f18: FieldDef[T18], f19: FieldDef[T19], f20: FieldDef[T20])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17], vs(17).asInstanceOf[T18], vs(18).asInstanceOf[T19], vs(19).asInstanceOf[T20]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17], f18: FieldDef[T18], f19: FieldDef[T19], f20: FieldDef[T20], f21: FieldDef[T21])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17], vs(17).asInstanceOf[T18], vs(18).asInstanceOf[T19], vs(19).asInstanceOf[T20], vs(20).asInstanceOf[T21]))

  def build[A, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](f1: FieldDef[T1], f2: FieldDef[T2], f3: FieldDef[T3], f4: FieldDef[T4], f5: FieldDef[T5], f6: FieldDef[T6], f7: FieldDef[T7], f8: FieldDef[T8], f9: FieldDef[T9], f10: FieldDef[T10], f11: FieldDef[T11], f12: FieldDef[T12], f13: FieldDef[T13], f14: FieldDef[T14], f15: FieldDef[T15], f16: FieldDef[T16], f17: FieldDef[T17], f18: FieldDef[T18], f19: FieldDef[T19], f20: FieldDef[T20], f21: FieldDef[T21], f22: FieldDef[T22])(construct: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => A): ConfigLoader[A] =
    new ConfigLoaderImpl[A](Seq(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22), vs => construct(vs(0).asInstanceOf[T1], vs(1).asInstanceOf[T2], vs(2).asInstanceOf[T3], vs(3).asInstanceOf[T4], vs(4).asInstanceOf[T5], vs(5).asInstanceOf[T6], vs(6).asInstanceOf[T7], vs(7).asInstanceOf[T8], vs(8).asInstanceOf[T9], vs(9).asInstanceOf[T10], vs(10).asInstanceOf[T11], vs(11).asInstanceOf[T12], vs(12).asInstanceOf[T13], vs(13).asInstanceOf[T14], vs(14).asInstanceOf[T15], vs(15).asInstanceOf[T16], vs(16).asInstanceOf[T17], vs(17).asInstanceOf[T18], vs(18).asInstanceOf[T19], vs(19).asInstanceOf[T20], vs(20).asInstanceOf[T21], vs(21).asInstanceOf[T22]))
}
