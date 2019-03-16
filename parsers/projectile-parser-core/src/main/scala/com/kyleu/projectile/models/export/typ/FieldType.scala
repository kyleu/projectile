package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._
import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class FieldType(override val value: String) extends StringEnumEntry {
  val className = getClass.getSimpleName.stripSuffix("$")
  override def toString = value
}

object FieldType extends StringEnum[FieldType] {
  implicit val encodeFieldType: Encoder[FieldType] = FieldTypeEncoder.encodeFieldType
  implicit val decodeFieldType: Decoder[FieldType] = FieldTypeDecoder.decodeFieldType

  case object UnitType extends FieldType("unit")

  case object StringType extends FieldType("string")
  case object EncryptedStringType extends FieldType("encrypted")

  case object NothingType extends FieldType("nothing")
  case object AnyType extends FieldType("any")
  case object BooleanType extends FieldType("boolean")
  case object ByteType extends FieldType("byte")
  case object ShortType extends FieldType("short")
  case object IntegerType extends FieldType("integer")
  case object LongType extends FieldType("long")
  case object FloatType extends FieldType("float")
  case object DoubleType extends FieldType("double")
  case object BigDecimalType extends FieldType("decimal")

  case object DateType extends FieldType("date")
  case object TimeType extends FieldType("time")
  case object TimestampType extends FieldType("timestamp")
  case object TimestampZonedType extends FieldType("timestamptz")

  case object RefType extends FieldType("ref")
  case object XmlType extends FieldType("xml")
  case object UuidType extends FieldType("uuid")

  case class EnumType(key: String) extends FieldType("enum")
  case class StructType(key: String, tParams: Seq[TypeParam] = Nil) extends FieldType("struct")
  case class ObjectType(key: String, fields: Seq[ObjectField], tParams: Seq[TypeParam] = Nil) extends FieldType("object")
  case class UnionType(key: String, types: Seq[FieldType]) extends FieldType("union")

  case class MethodType(params: Seq[ObjectField], ret: FieldType) extends FieldType("method")

  case class ListType(typ: FieldType) extends FieldType("list")
  case class SetType(typ: FieldType) extends FieldType("set")
  case class MapType(k: FieldType, v: FieldType) extends FieldType("set")

  case class ExoticType(key: String) extends FieldType("exotic")

  case object JsonType extends FieldType("json")
  case object CodeType extends FieldType("code")
  case object TagsType extends FieldType("hstore")

  case object ByteArrayType extends FieldType("byteArray")

  override def values = findValues
  val scalars: Set[FieldType] = Set(BooleanType, StringType, IntegerType, LongType, DoubleType)
}
