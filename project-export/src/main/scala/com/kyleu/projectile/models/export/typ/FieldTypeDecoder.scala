package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.HCursor

import scala.util.control.NonFatal

object FieldTypeDecoder {
  private[this] val listTypeDecoder: Decoder[ListType] = (c: HCursor) => Right(ListType(
    typ = decodeFieldType.apply(c.downField("typ").asInstanceOf[HCursor]).right.get
  ))

  private[this] val setTypeDecoder: Decoder[SetType] = (c: HCursor) => Right(SetType(
    typ = decodeFieldType.apply(c.downField("typ").asInstanceOf[HCursor]).right.get
  ))

  private[this] val mapTypeDecoder: Decoder[MapType] = (c: HCursor) => Right(MapType(
    k = decodeFieldType.apply(c.downField("k").asInstanceOf[HCursor]).right.get,
    v = decodeFieldType.apply(c.downField("v").asInstanceOf[HCursor]).right.get
  ))

  private[this] val enumTypeDecoder: Decoder[EnumType] = (c: HCursor) => Right(EnumType(key = c.downField("key").as[String].right.get))

  private[this] val structTypeDecoder: Decoder[StructType] = (c: HCursor) => Right(StructType(key = c.downField("key").as[String].right.get))

  private[this] val objectTypeDecoder: Decoder[ObjectType] = (c: HCursor) => Right(ObjectType(
    key = c.downField("key").as[String].right.get,
    fields = c.downField("fields").as[Seq[ObjectField]].right.get
  ))

  implicit def decodeFieldType: Decoder[FieldType] = (c: HCursor) => try {
    val t = c.downField("t").as[String].getOrElse(c.as[String].getOrElse(throw new IllegalStateException("Encountered field type without \"t\" attribute.")))
    t match {
      case StringType.value => Right(StringType)
      case EncryptedStringType.value => Right(EncryptedStringType)

      case BooleanType.value => Right(BooleanType)
      case ByteType.value => Right(ByteType)
      case ShortType.value => Right(ShortType)
      case IntegerType.value => Right(IntegerType)
      case LongType.value => Right(LongType)
      case FloatType.value => Right(FloatType)
      case DoubleType.value => Right(DoubleType)
      case BigDecimalType.value => Right(BigDecimalType)

      case DateType.value => Right(DateType)
      case TimeType.value => Right(TimeType)
      case TimestampType.value => Right(TimestampType)
      case TimestampZonedType.value => Right(TimestampZonedType)

      case RefType.value => Right(RefType)
      case XmlType.value => Right(XmlType)
      case UuidType.value => Right(UuidType)

      case "object" => objectTypeDecoder.apply(c)
      case "struct" => structTypeDecoder.apply(c)

      case "enum" => enumTypeDecoder.apply(c)
      case "list" => listTypeDecoder.apply(c)
      case "set" => setTypeDecoder.apply(c)
      case "map" => mapTypeDecoder.apply(c)

      case JsonType.value => Right(JsonType)
      case CodeType.value => Right(CodeType)
      case TagsType.value => Right(TagsType)

      case ByteArrayType.value => Right(ByteArrayType)
    }
  } catch {
    case NonFatal(x) => throw new IllegalStateException("Error parsing: " + printJson(c.value), x)
  }
}
