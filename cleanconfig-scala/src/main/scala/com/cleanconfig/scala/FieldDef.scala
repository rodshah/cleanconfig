package com.cleanconfig.scala

import com.cleanconfig.core.validation.ValidationRule

/**
 * Represents a single field binding between a configuration property key
 * and a case class constructor parameter.
 *
 * Three variants exist:
 *  - [[RequiredFieldDef]] — property must be present (unless a default is set)
 *  - [[OptionalFieldDef]] — missing property yields `None`, present yields `Some(value)`
 *  - [[NestedFieldDef]]   — delegates to another [[ConfigLoader]] with key prefix stripping
 *  - [[ListFieldDef]]     — parses a list from HOCON inline syntax or indexed keys
 *
 * @tparam T the type produced by this field (matches the case class parameter type)
 */
sealed trait FieldDef[T]

/**
 * A required configuration field.
 *
 * If the property key is absent and no default is provided, validation fails.
 * Use [[withDefault]] to make the field optional with a fallback value.
 *
 * {{{
 * field[Int]("server.port", Rules.port).withDefault(8080)
 * }}}
 *
 * @param name      property key in the configuration map
 * @param fieldType typeclass providing the Java class for type conversion
 * @param rule      optional validation rule applied after type conversion
 * @param default   optional default value used when the property is absent
 * @tparam T the Scala type of the field value
 */
case class RequiredFieldDef[T](
    name: String,
    fieldType: FieldType[T],
    rule: Option[ValidationRule[_]] = None,
    default: Option[T] = None
) extends FieldDef[T] {

  /** Returns a copy with the given default value. */
  def withDefault(value: T): RequiredFieldDef[T] = copy(default = Some(value))
}

/**
 * An optional configuration field that produces `Option[T]`.
 *
 * Missing property yields `None` (no error). Present property is converted
 * and validated normally, yielding `Some(value)` on success.
 *
 * {{{
 * optional[String]("app.description", Rules.maxLength(200))
 * }}}
 *
 * @param name      property key in the configuration map
 * @param fieldType typeclass providing the Java class for type conversion
 * @param rule      optional validation rule applied when the property is present
 * @tparam T the inner type (the field in the case class should be `Option[T]`)
 */
case class OptionalFieldDef[T](
    name: String,
    fieldType: FieldType[T],
    rule: Option[ValidationRule[_]] = None
) extends FieldDef[Option[T]]

/**
 * A nested configuration field that delegates to another [[ConfigLoader]].
 *
 * Keys in the input map are filtered by `prefix` and the prefix is stripped
 * before passing to the nested loader.
 *
 * '''Limitation:''' The nested loader receives only the prefix-stripped subset
 * of the properties map. Cross-property conditions (e.g.,
 * `Conditions.propertyEquals("parent.key", "value")`) will not see keys
 * outside the nested scope.
 *
 * {{{
 * // "db.url" and "db.max.pool" become "url" and "max.pool" for DbConfig loader
 * nested[DbConfig]("db.")
 * }}}
 *
 * @param prefix prefix to filter and strip from keys (e.g., `"db."`)
 * @param loader the [[ConfigLoader]] for the nested case class
 * @tparam T the nested case class type
 */
case class NestedFieldDef[T](
    prefix: String,
    loader: ConfigLoader[T]
) extends FieldDef[T]

/**
 * A list configuration field that produces `List[T]`.
 *
 * Supports two input formats (checked in order):
 *  1. '''HOCON inline''' — the key maps to a value like `["a", "b", "c"]` or
 *     a comma-separated string `a, b, c` (wrapped in brackets and parsed via
 *     Typesafe Config).
 *  2. '''Indexed keys''' — the map contains `key.0`, `key.1`, etc. (produced
 *     by `HoconPropertySource` flattening).
 *
 * Each element is individually converted via `TypeConverterRegistry` and
 * optionally validated with `elementRule`. Element errors reference the
 * position: `"items[2]"`.
 *
 * '''Note:''' An empty list `[]` is valid and returns `List.empty` — the
 * `elementRule` is not invoked when there are no elements. If you need
 * a non-empty constraint, validate the `List` after loading.
 *
 * {{{
 * listField[String]("allowed.topics", Rules.notBlank)
 * listField[Int]("partition.sizes", Rules.positive).withDefault(List(128, 256))
 * }}}
 *
 * @param name        property key in the configuration map
 * @param fieldType   typeclass providing the Java class for element type conversion
 * @param elementRule optional validation rule applied to each element
 * @param default     optional default value used when the property is absent
 * @tparam T the element type (the case class field should be `List[T]`)
 */
case class ListFieldDef[T](
    name: String,
    fieldType: FieldType[T],
    elementRule: Option[ValidationRule[_]] = None,
    default: Option[List[T]] = None
) extends FieldDef[List[T]] {

  /** Returns a copy with the given default list value. */
  def withDefault(value: List[T]): ListFieldDef[T] = copy(default = Some(value))
}
