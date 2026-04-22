package com.cleanconfig.scala

/**
 * Typeclass that maps Scala types to the Java `Class[_]` used by
 * `TypeConverterRegistry` for String-to-T conversion.
 *
 * Scala primitives (`Int`, `Long`, etc.) must map to their Java boxed
 * counterparts (`java.lang.Integer`, `java.lang.Long`) because the
 * converter registry registers converters under the boxed class, not
 * the primitive class (`Integer.TYPE`).
 *
 * {{{
 * // Resolved automatically via context bound:
 * def field[T: FieldType](name: String): RequiredFieldDef[T]
 * }}}
 *
 * @tparam T the Scala type
 */
trait FieldType[T] {

  /** The Java class used to look up the converter in `TypeConverterRegistry`. */
  def javaClass: Class[_]
}

object FieldType {

  def apply[T](implicit ft: FieldType[T]): FieldType[T] = ft

  private def instance[T](cls: Class[_]): FieldType[T] =
    new FieldType[T] { val javaClass: Class[_] = cls }

  // Scala primitives → Java boxed types
  implicit val intFieldType: FieldType[Int] = instance[Int](classOf[java.lang.Integer])
  implicit val longFieldType: FieldType[Long] = instance[Long](classOf[java.lang.Long])
  implicit val doubleFieldType: FieldType[Double] = instance[Double](classOf[java.lang.Double])
  implicit val floatFieldType: FieldType[Float] = instance[Float](classOf[java.lang.Float])
  implicit val booleanFieldType: FieldType[Boolean] = instance[Boolean](classOf[java.lang.Boolean])
  implicit val shortFieldType: FieldType[Short] = instance[Short](classOf[java.lang.Short])
  implicit val byteFieldType: FieldType[Byte] = instance[Byte](classOf[java.lang.Byte])

  // Identity mapping
  implicit val stringFieldType: FieldType[String] = instance[String](classOf[java.lang.String])

  // Fallback for any other type (URL, URI, Path, Duration, Instant, etc.)
  implicit def fromClassTag[T](implicit ct: scala.reflect.ClassTag[T]): FieldType[T] =
    instance[T](ct.runtimeClass)
}
