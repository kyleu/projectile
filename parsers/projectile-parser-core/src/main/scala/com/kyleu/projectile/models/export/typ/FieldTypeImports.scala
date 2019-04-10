package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.models.output.CommonImportHelper

object FieldTypeImports {
  def importTypes(config: ExportConfiguration, isJs: Boolean = false, isThrift: Boolean = false)(types: FieldType*): Seq[Seq[String]] = {
    types.distinct.flatMap(imports(config, _, isJs, isThrift))
  }

  def imports(config: ExportConfiguration, t: FieldType, isJs: Boolean = false, isThrift: Boolean = false): Seq[Seq[String]] = t match {
    case UuidType => Seq(Seq("java", "util") :+ FieldTypeAsScala.asScala(config, t, isJs, isThrift))
    case DateType | TimeType | TimestampType | TimestampZonedType => Seq(Seq("java", "time") :+ FieldTypeAsScala.asScala(config, t, isJs, isThrift))
    case JsonType => Seq(Seq("io", "circe") :+ FieldTypeAsScala.asScala(config, t, isJs, isThrift))
    case TagsType => Seq(CommonImportHelper.get(config, "Tag")._1 :+ CommonImportHelper.get(config, "Tag")._2)
    case EnumType(key) => config.getEnumOpt(key).map(_.modelPackage(config) :+ FieldTypeAsScala.asScala(config, t, isJs, isThrift)).toSeq
    case StructType(key, tp) =>
      config.getModelOpt(key).map(_.modelPackage(config) :+ FieldTypeAsScala.asScala(config, t, isJs, isThrift)).toSeq ++ tp.flatMap { x =>
        x.constraint.toSeq.flatMap(x => imports(config, x, isJs, isThrift))
      }
    case ListType(typ) => imports(config, typ, isJs, isThrift)
    case SetType(typ) => imports(config, typ, isJs, isThrift)
    case MapType(k, v) => importTypes(config, isJs, isThrift)(k, v)
    case UnionType(_, v) if isJs => Seq("scala", "scalajs", "js", "|") +: v.flatMap(imports(config, _, isJs, isThrift))
    case UnionType(k, _) if isThrift => config.getUnionOpt(k).map(u => u.pkg :+ u.className).toSeq

    case IntersectionType(_, v) if isJs => Seq("scala", "scalajs", "js", "|") +: v.flatMap(imports(config, _, isJs, isThrift))
    case MethodType(params, ret) => importTypes(config, isJs, isThrift)(params.map(_.t): _*) ++ imports(config, ret, isJs, isThrift)

    case _ => Nil
  }
}
