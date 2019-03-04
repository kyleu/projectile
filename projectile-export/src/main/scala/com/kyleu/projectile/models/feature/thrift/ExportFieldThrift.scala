package com.kyleu.projectile.models.feature.thrift

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.typ.FieldType._

object ExportFieldThrift {
  def thriftType(t: FieldType, config: ExportConfiguration): String = t match {
    case UnitType => "void"

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

    case EnumType(key) => config.getEnumOpt(key) match {
      case Some(e) => e.className
      case None => "string"
    }
    case StructType(key) => config.getModelOpt(key) match {
      case Some(m) => m.className
      case None => "string"
    }
    case ObjectType(_, _) => throw new IllegalStateException("Object types are not supported in Thrift")
    case MethodType(_, _) => throw new IllegalStateException("Method types are not supported in Thrift")

    case ListType(typ) => s"list<${thriftType(typ, config)}>"
    case SetType(typ) => s"set<${thriftType(typ, config)}>"
    case MapType(k, v) => s"map<${thriftType(k, config)}, ${thriftType(v, config)}>"
    case UnionType(k, _) => config.getUnionOpt(k) match {
      case Some(u) => u.className
      case None => "string"
    }

    case JsonType => "string"
    case CodeType => "string"
    case TagsType => "list<common.Tag>"

    case ByteArrayType => "binary"

    case typ => throw new IllegalStateException(s"[$typ] is not currently supported in Thrift")
  }
}
