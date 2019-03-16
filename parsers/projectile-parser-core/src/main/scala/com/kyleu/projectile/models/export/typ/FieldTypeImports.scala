package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.models.output.CommonImportHelper

object FieldTypeImports {
  def imports(config: ExportConfiguration, t: FieldType, isJs: Boolean = true): Seq[Seq[String]] = t match {
    case UuidType => Seq(Seq("java", "util") :+ FieldTypeAsScala.asScala(config, t))
    case DateType | TimeType | TimestampType | TimestampZonedType => Seq(Seq("java", "time") :+ FieldTypeAsScala.asScala(config, t))
    case JsonType => Seq(Seq("io", "circe") :+ FieldTypeAsScala.asScala(config, t))
    case TagsType => Seq(CommonImportHelper.get(config, "Tag")._1 :+ CommonImportHelper.get(config, "Tag")._2)
    case EnumType(key) => config.getEnumOpt(key).map(_.modelPackage(config) :+ FieldTypeAsScala.asScala(config, t)).toSeq
    case StructType(key, tp) => config.getModelOpt(key).map(_.modelPackage(config) :+ FieldTypeAsScala.asScala(config, t)).toSeq ++ tp.flatMap { x =>
      x.constraint.toSeq.flatMap(x => imports(config, x))
    }
    case ListType(typ) => imports(config, typ)
    case SetType(typ) => imports(config, typ)
    case MapType(k, v) => imports(config, k) ++ imports(config, v)
    case UnionType(k, v) if isJs => Seq("scala", "scalajs", "js", "|") +: v.flatMap(imports(config, _))

    case _ => Nil
  }
}
