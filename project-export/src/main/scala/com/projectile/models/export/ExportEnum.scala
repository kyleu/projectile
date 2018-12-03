package com.projectile.models.export

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.ExportHelper
import com.projectile.models.feature.EnumFeature
import com.projectile.models.project.member.EnumMember
import com.projectile.models.project.member.EnumMember.InputType
import com.projectile.util.JsonSerializers._

object ExportEnum {
  implicit val jsonEncoder: Encoder[ExportEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportEnum] = deriveDecoder
}

case class ExportEnum(
    inputType: InputType,
    pkg: List[String] = Nil,
    key: String,
    className: String,
    values: Seq[String],
    features: Set[EnumFeature] = Set.empty
) {
  def apply(m: EnumMember) = copy(
    pkg = m.pkg.toList,
    className = m.getOverride("className", ExportHelper.toClassName(ExportHelper.toIdentifier(m.key))),
    values = values.filterNot(m.ignored.contains),
    features = m.features
  )

  val valuesWithClassNames = values.map { v =>
    val newVal = v.indexOf(':') match {
      case -1 => v
      case x => v.substring(x + 1)
    }
    v -> ExportHelper.toClassName(ExportHelper.toIdentifier(newVal))
  }

  val modelPkg = pkg.lastOption match {
    case Some("models") => pkg
    case _ => "models" +: pkg
  }

  val propertyName = ExportHelper.toIdentifier(className)

  val modelPackage = List("models") ++ pkg
  val slickPackage = List("models", "table") ++ pkg
  val doobiePackage = List("models", "doobie") ++ pkg

  val controllerPackage = List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })

  def fullClassPath(config: ExportConfiguration) = (config.applicationPackage ++ modelPackage :+ className).mkString(".")
}
