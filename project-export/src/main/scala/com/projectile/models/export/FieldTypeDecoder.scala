package com.projectile.models.export

import com.projectile.models.export.FieldType._
import com.projectile.util.JsonSerializers._
import io.circe.HCursor

object FieldTypeDecoder {
  private[this] implicit val listTypeDecoder: Decoder[ListType] = deriveDecoder

  val decodeFieldType: Decoder[FieldType] = (c: HCursor) => {
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

      case ObjectType.value => Right(ObjectType)
      case StructType.value => Right(StructType)

      case EnumType.value => Right(EnumType)
      case "list" => listTypeDecoder.apply(c)

      case JsonType.value => Right(JsonType)
      case CodeType.value => Right(CodeType)
      case TagsType.value => Right(TagsType)

      case ByteArrayType.value => Right(ByteArrayType)
      case ComplexType.value => Right(ComplexType)
      case UnknownType.value => Right(UnknownType)
    }
  }
}
