package com.kyleu.projectile.models.feature.doobie

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.typ.FieldType._

object DoobieImports {
  def imports(config: ExportConfiguration, t: FieldType): Seq[Seq[String]] = t match {
    case EnumType(key) =>
      val e = config.getEnum(key, "doobieImports")
      Seq(config.getEnum(key, "imports").doobiePackage :+ (e.className + "Doobie") :+ (e.propertyName + "Meta"))

    case ListType(typ) => imports(config, typ)
    case SetType(typ) => imports(config, typ)
    case MapType(k, v) => imports(config, k) ++ imports(config, v)
    case _ => Nil
  }
}
