package com.projectile.models.feature.thrift

import com.projectile.models.export.FieldType
import com.projectile.models.export.FieldType._
import com.projectile.models.export.ExportEnum

object ExportFieldThrift {
  def thriftType(t: FieldType, nativeType: String, enumOpt: Option[ExportEnum]): String = t match {
    case StringType => "string"
    case EncryptedStringType => "string"

    case BooleanType => "bool"
    case ByteType => "byte"
    case ShortType => "common.int"
    case IntegerType => "common.int"
    case LongType => "common.long"
    case FloatType => "double"
    case DoubleType => "double"
    case BigDecimalType => "common.BigDecimal"

    case DateType => "common.LocalDate"
    case TimeType => "common.LocalTime"
    case TimestampType => "common.LocalDateTime"
    case TimestampZonedType => "common.ZonedDateTime"

    case RefType => "string"
    case XmlType => "string"
    case UuidType => "common.UUID"

    case ObjectType => "string"
    case StructType => "string"

    case ListType(typ) => s"list<${thriftType(typ, "", enumOpt)}>"
    case EnumType => enumOpt match {
      case Some(_) => "string"
      case None => throw new IllegalStateException(s"Cannot load enum.")
    }

    case JsonType => "string"
    case CodeType => "string"
    case TagsType => "list<common.Tag>"

    case ByteArrayType => "binary"

    case ComplexType => throw new IllegalStateException("TODO")
    case UnknownType => "string"
  }
}
