package com.projectile.models.export.typ

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType._

object FieldTypeImports {
  def imports(config: ExportConfiguration, t: FieldType): Seq[Seq[String]] = t match {
    case UuidType => Seq(Seq("java", "util") :+ FieldTypeAsScala.asScala(config, t))
    case DateType | TimeType | TimestampType | TimestampZonedType => Seq(Seq("java", "time") :+ FieldTypeAsScala.asScala(config, t))
    case JsonType => Seq(Seq("io", "circe") :+ FieldTypeAsScala.asScala(config, t))
    case EnumType(key) => Seq(config.applicationPackage ++ Seq("models") ++ config.getEnum(key).pkg :+ FieldTypeAsScala.asScala(config, t))
    case ListType(typ) => imports(config, typ)
    case SetType(typ) => imports(config, typ)
    case MapType(k, v) => imports(config, k) ++ imports(config, v)
    case _ => Nil
  }
}
