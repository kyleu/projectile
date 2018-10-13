package models.export

import models.database.schema.ColumnType.{ArrayType, BigDecimalType, BooleanType, ByteArrayType, ByteType, CodeType, DateType, DoubleType, EncryptedStringType, EnumType, FloatType, IntegerType, JsonType, LongType, ObjectType, RefType, ShortType, StringType, StructType, TagsType, TimeType, TimestampType, TimestampZonedType, UnknownType, UuidType, XmlType}

object ExportFieldGraphQL {
  private[this] def graphQLType(field: ExportField) = field.t match {
    case StringType => "StringType"
    case EncryptedStringType => "StringType"

    case BooleanType => "BooleanType"
    case ByteType => "byteType"
    case ShortType => "IntType"
    case IntegerType => "IntType"
    case LongType => "LongType"
    case FloatType => "FloatType"
    case DoubleType => "DoubleType"
    case BigDecimalType => "BigDecimalType"

    case DateType => "localDateType"
    case TimeType => "localTimeType"
    case TimestampType => "localDateTimeType"
    case TimestampZonedType => "zonedDateTimeType"

    case RefType => "StringType"
    case XmlType => "StringType"
    case UuidType => "uuidType"

    case ObjectType => "StringType"
    case StructType => "StringType"
    case JsonType => "JsonType"

    case EnumType => field.enumOpt match {
      case Some(enum) => enum.propertyName + "EnumType"
      case None => throw new IllegalStateException(s"Cannot load enum for field [${field.propertyName}].")
    }
    case CodeType => "StringType"
    case TagsType => "TagsType"

    case ByteArrayType => "ArrayType(StringType)"
    case ArrayType => field.sqlTypeName match {
      case x if x.startsWith("_int") => "ArrayType(IntType)"
      case x if x.startsWith("_uuid") => "ArrayType(uuidType)"
      case _ => "ArrayType(StringType)"
    }

    case UnknownType => "UnknownType"
  }

  def argType(field: ExportField) = if (field.notNull) { graphQLType(field) } else { "OptionInputType(" + graphQLType(field) + ")" }
  def listArgType(field: ExportField) = "ListInputType(" + graphQLType(field) + ")"
}
