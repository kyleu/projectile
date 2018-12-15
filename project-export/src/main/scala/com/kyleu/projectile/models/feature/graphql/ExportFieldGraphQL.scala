package com.kyleu.projectile.models.feature.graphql

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._

object ExportFieldGraphQL {
  private[this] def graphQLType(config: ExportConfiguration, field: ExportField): String = field.t match {
    case UnitType => "StringType"

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

    case ObjectType(_, _) => throw new IllegalStateException("TODO: Objects")
    case StructType(key) => config.getModelOpt(key) match {
      case Some(_) => throw new IllegalStateException("TODO: Struct types")
      case None => "StringType"
    }

    case EnumType(key) => config.getEnumOpt(key) match {
      case Some(enum) => enum.propertyName + "EnumType"
      case None => throw new IllegalStateException(s"Cannot load enum for field [${field.propertyName}].")
    }
    case ListType(typ) => s"ListType(${graphQLType(config, field.copy(t = typ))})"
    case SetType(typ) => s"ListType(${graphQLType(config, field.copy(t = typ))})"
    case MapType(_, _) => throw new IllegalStateException("Maps are not supported in GraphQL")

    case JsonType => "JsonType"
    case CodeType => "StringType"
    case TagsType => "TagsType"
    case ByteArrayType => "ArrayType(StringType)"
  }

  def argType(config: ExportConfiguration, field: ExportField) = if (field.required) {
    graphQLType(config, field)
  } else {
    "OptionInputType(" + graphQLType(config, field) + ")"
  }

  def listArgType(config: ExportConfiguration, field: ExportField) = "ListInputType(" + graphQLType(config, field) + ")"
}
