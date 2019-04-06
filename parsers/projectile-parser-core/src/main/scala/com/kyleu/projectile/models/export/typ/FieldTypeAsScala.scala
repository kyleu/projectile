package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._

object FieldTypeAsScala {
  def asScala(config: ExportConfiguration, t: FieldType, isJs: Boolean = false): String = t match {
    case UnitType => "Unit"

    case StringType => "String"
    case EncryptedStringType => "String"

    case BooleanType => "Boolean"
    case ByteType => "Byte"
    case ShortType => "Short"
    case IntegerType => "Int"
    case LongType => "Long"
    case FloatType => "Float"
    case DoubleType => "Double"
    case BigDecimalType => "BigDecimal"

    case DateType => "LocalDate"
    case TimeType => "LocalTime"
    case TimestampType => "LocalDateTime"
    case TimestampZonedType => "ZonedDateTime"

    case RefType => "String"
    case XmlType => "String"
    case UuidType => "UUID"

    case ObjectType(k, _, tp) => k + typeParamsString(config, tp, isJs)
    case StructType(key, tp) => config.getModelOpt(key).map(_.className).getOrElse(key) + typeParamsString(config, tp, isJs)

    case EnumType(key) => config.getEnumOpt(key).map(_.className).getOrElse(key)
    case ListType(typ) if isJs => s"js.Array[${asScala(config, typ, isJs)}]"
    case ListType(typ) => s"List[${asScala(config, typ, isJs)}]"
    case SetType(typ) => s"Set[${asScala(config, typ, isJs)}]"
    case MapType(k, v) => s"Map[${asScala(config, k, isJs)}, ${asScala(config, v, isJs)}]"

    case UnionType(_, v) if isJs => v.map(x => asScala(config, x, isJs)).mkString(" | ")
    case UnionType(_, _) => "Json"

    case IntersectionType(_, v) if isJs => v.map(x => asScala(config, x, isJs)).mkString(" with ")
    case IntersectionType(_, _) => "Json"

    case MethodType(params, ret) if isJs => s"js.Function${params.length}[${params.map(p => asScala(config, p.t, isJs)).mkString(", ")}, ${asScala(config, ret, isJs)}]"
    case MethodType(params, ret) => s"(${params.map(p => asScala(config, p.t, isJs)).mkString(", ")}): ${asScala(config, ret, isJs)}"

    case JsonType => "Json"
    case CodeType => "String"
    case TagsType => "List[Tag]"

    case ByteArrayType => "Array[Byte]"

    case NothingType => "Nothing"

    case AnyType if isJs => "js.Any"
    case AnyType => "Any"

    case ThisType => "this.type"

    case ExoticType(key) => key match {
      case _ => s"js.Any /* exotic($key) */"
    }
  }

  private[this] def typeParamsString(config: ExportConfiguration, tp: Seq[TypeParam], isJs: Boolean) = tp.toList match {
    case Nil => ""
    case _ => "[" + tp.map { p =>
      p.name + p.constraint.map(c => s" <: " + asScala(config, c, isJs)).getOrElse("") + p.default.map(" = " + _).getOrElse("")
    }.mkString(", ") + "]"
  }
}
