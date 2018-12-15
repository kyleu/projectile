package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._

object FieldTypeFromString {
  def fromString(config: ExportConfiguration, t: FieldType, s: String): String = t match {
    case UnitType => "()"

    case BooleanType => s + " == \"true\""
    case ByteType => s + ".toInt.toByte"
    case ShortType => s + ".toInt.toShort"
    case IntegerType => s + ".toInt"
    case LongType => s + ".toLong"
    case FloatType => s + ".toFloat"
    case DoubleType => s + ".toDouble"
    case BigDecimalType => s"BigDecimal($s)"

    case DateType => s + s"DateUtils.fromDateString($s)"
    case TimeType => s + s"DateUtils.fromTimeString($s)"
    case TimestampType => s + s"DateUtils.fromIsoString($s)"
    case TimestampZonedType => s + s"DateUtils.fromIsoStringZoned($s)"

    case UuidType => s"UUID.fromString($s)"

    case EnumType(key) => s"${config.getEnum(key, "fromString").className}.withValue($s)"
    case ListType(_) => s + ".TODO"
    case SetType(_) => s + ".TODO"
    case MapType(_, _) => s + ".TODO"

    case JsonType => s"util.JsonSerializers.toJson($s)"
    case TagsType => s"Tag.seqFromString($s)"

    case ByteArrayType => s + ".split(\",\").map(_.toInt.toByte)"

    case _ => s
  }
}
