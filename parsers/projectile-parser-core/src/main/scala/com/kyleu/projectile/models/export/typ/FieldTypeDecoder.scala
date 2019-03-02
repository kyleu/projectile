package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.util.JacksonUtils
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.HCursor

import scala.util.control.NonFatal

object FieldTypeDecoder {
  private[this] def extract[T](e: Either[Throwable, T]) = e match {
    case Left(x) => throw x
    case Right(x) => x
  }

  private[this] val listTypeDecoder: Decoder[ListType] = (c: HCursor) => Right(ListType(
    typ = extract(decodeFieldType.apply(c.downField("typ").asInstanceOf[HCursor]))
  ))

  private[this] val setTypeDecoder: Decoder[SetType] = (c: HCursor) => Right(SetType(
    typ = extract(decodeFieldType.apply(c.downField("typ").asInstanceOf[HCursor]))
  ))

  private[this] val mapTypeDecoder: Decoder[MapType] = (c: HCursor) => Right(MapType(
    k = extract(decodeFieldType.apply(c.downField("k").asInstanceOf[HCursor])),
    v = extract(decodeFieldType.apply(c.downField("v").asInstanceOf[HCursor]))
  ))

  private[this] val enumTypeDecoder: Decoder[EnumType] = (c: HCursor) => Right(EnumType(key = extract(c.downField("key").as[String])))

  private[this] val structTypeDecoder: Decoder[StructType] = (c: HCursor) => Right(StructType(key = extract(c.downField("key").as[String])))

  private[this] val objectTypeDecoder: Decoder[ObjectType] = (c: HCursor) => Right(ObjectType(
    key = extract(c.downField("key").as[String]),
    fields = extract(c.downField("fields").as[Seq[ObjectField]])
  ))

  implicit def decodeFieldType: Decoder[FieldType] = (c: HCursor) => try {
    val t = c.downField("t").as[String].getOrElse(c.as[String].getOrElse(throw new IllegalStateException("Encountered field type without \"t\" attribute")))
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
    case NonFatal(x) => throw new IllegalStateException("Error parsing: " + JacksonUtils.printJackson(c.value), x)
  }
}
