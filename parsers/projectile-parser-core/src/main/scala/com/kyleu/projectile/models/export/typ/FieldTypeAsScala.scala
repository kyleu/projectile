package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._

object FieldTypeAsScala {
  def asScala(config: ExportConfiguration, t: FieldType, isThrift: Boolean = false, isJs: Boolean = false): String = t match {
    case UnitType => "Unit"

    case StringType => "String"
    case EncryptedStringType => "String"

    case BooleanType => "Boolean"
    case ByteType => "Byte"
    case ShortType => "Short"
    case IntegerType => "Int"
    case LongType => "Long"
    case SerialType => "Long"
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
    case ListType(typ) if isJs => s"js.Array[${asScala(config = config, t = typ, isThrift = isThrift, isJs = isJs)}]"
    case ListType(typ) => s"List[${asScala(config, typ, isThrift = isThrift, isJs = isJs)}]"
    case SetType(typ) => s"Set[${asScala(config, typ, isThrift = isThrift, isJs = isJs)}]"
    case MapType(k, v) => s"Map[${asScala(config, k, isThrift = isThrift, isJs = isJs)}, ${asScala(config, v, isThrift = isThrift, isJs = isJs)}]"

    case UnionType(_, v) if isJs => v.map(x => asScala(config, x, isThrift = isThrift, isJs = isJs)).mkString(" | ")
    case UnionType(key, _) if isThrift => key
    case UnionType(_, _) => "Json"

    case IntersectionType(_, v) if isJs => v.map(x => asScala(config, x, isThrift = isThrift, isJs = isJs)).mkString(" with ")
    case IntersectionType(_, _) => "Json"

    case MethodType(params, ret) if isJs =>
      val paramsString = params.map { p =>
        val sc = asScala(config, p.t, isThrift = isThrift, isJs = isJs)
        if (p.k.toLowerCase == sc.toLowerCase) { sc } else { s"/* ${p.k}: */ $sc" }
      }.mkString(", ")
      s"js.Function${params.length}[$paramsString, ${asScala(config, ret, isThrift = isThrift, isJs = isJs)}]"
    case MethodType(params, ret) =>
      val paramsString = params.map(p => asScala(config, p.t, isThrift = isThrift, isJs = isJs)).mkString(", ")
      s"($paramsString): ${asScala(config, ret, isThrift = isThrift, isJs = isJs)}"

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
