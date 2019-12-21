package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._
import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class FieldType(override val value: String, val isScalar: Boolean = false, val isDate: Boolean = false) extends StringEnumEntry {
  val className = getClass.getSimpleName.stripSuffix("$")
  override def toString = value
}

object FieldType extends StringEnum[FieldType] {
  implicit val encodeFieldType: Encoder[FieldType] = FieldTypeEncoder.encodeFieldType
  implicit val decodeFieldType: Decoder[FieldType] = FieldTypeDecoder.decodeFieldType

  case object UnitType extends FieldType("unit")

  case object StringType extends FieldType("string", isScalar = true)
  case object EncryptedStringType extends FieldType("encrypted", isScalar = true)

  case object NothingType extends FieldType("nothing")
  case object AnyType extends FieldType("any")
  case object ThisType extends FieldType("this")

  case object BooleanType extends FieldType("boolean", isScalar = true)
  case object ByteType extends FieldType("byte", isScalar = true)
  case object ShortType extends FieldType("short", isScalar = true)
  case object IntegerType extends FieldType("integer", isScalar = true)
  case object LongType extends FieldType("long", isScalar = true)
  case object FloatType extends FieldType("float", isScalar = true)
  case object DoubleType extends FieldType("double", isScalar = true)
  case object BigDecimalType extends FieldType("decimal")

  case object DateType extends FieldType("date", isDate = true)
  case object TimeType extends FieldType("time", isDate = true)
  case object TimestampType extends FieldType("timestamp", isDate = true)
  case object TimestampZonedType extends FieldType("timestamptz", isDate = true)

  case object RefType extends FieldType("ref")
  case object XmlType extends FieldType("xml")
  case object UuidType extends FieldType("uuid")

  final case class EnumType(key: String) extends FieldType("enum")
  final case class StructType(key: String, tParams: Seq[TypeParam] = Nil) extends FieldType("struct")
  final case class ObjectType(key: String, fields: Seq[ObjectField], tParams: Seq[TypeParam] = Nil) extends FieldType("object")

  final case class IntersectionType(key: String, types: Seq[FieldType]) extends FieldType("intersection")
  final case class UnionType(key: String, types: Seq[FieldType]) extends FieldType("union")

  final case class MethodType(params: Seq[ObjectField], ret: FieldType) extends FieldType("method")

  final case class ListType(typ: FieldType) extends FieldType("list")
  final case class SetType(typ: FieldType) extends FieldType("set")
  final case class MapType(k: FieldType, v: FieldType) extends FieldType("set")

  final case class ExoticType(key: String) extends FieldType("exotic")
  case object SerialType extends FieldType("serial", isScalar = true)

  case object JsonType extends FieldType("json")
  case object CodeType extends FieldType("code")
  case object TagsType extends FieldType("hstore")

  case object ByteArrayType extends FieldType("byteArray")

  override def values = findValues
}
