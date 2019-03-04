package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.project.member.EnumMember
import com.kyleu.projectile.util.JsonSerializers._

object ExportEnum {
  object EnumVal {
    implicit val jsonEncoder: Encoder[EnumVal] = deriveEncoder
    implicit val jsonDecoder: Decoder[EnumVal] = deriveDecoder
  }

  case class EnumVal(k: String, i: Option[Int] = None, s: Option[String] = None) {
    def className = ExportHelper.toClassName(k)
    def v = i.map(_.toString).orElse(s).getOrElse(k)
    override val toString = k + i.map(":" + _).getOrElse("") + s.map(":" + _).getOrElse("")
  }

  implicit val jsonEncoder: Encoder[ExportEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportEnum] = deriveDecoder
}

case class ExportEnum(
    inputType: InputType.Enum,
    pkg: List[String] = Nil,
    key: String,
    className: String,
    values: Seq[ExportEnum.EnumVal],
    features: Set[EnumFeature] = Set.empty
) {
  def apply(m: EnumMember) = copy(
    pkg = m.pkg.toList,
    className = m.getOverride("className", className),
    values = values.filterNot(v => m.ignored.contains(v.k)),
    features = m.features
  )

  val propertyName = ExportHelper.toIdentifier(className)
  lazy val firstVal = values.headOption.getOrElse(throw new IllegalStateException(s"Enum [$key] has no values"))

  def fullClassPath(config: ExportConfiguration) = (modelPackage(config) :+ className).mkString(".")

  def graphqlPackage(config: ExportConfiguration) = if (inputType.isThrift) {
    pkg
  } else {
    config.applicationPackage ++ List("models", "graphql") ++ pkg
  }
  def slickPackage(config: ExportConfiguration) = config.applicationPackage ++ List("models", "table") ++ pkg
  def doobiePackage(config: ExportConfiguration) = config.applicationPackage ++ List("models", "doobie") ++ pkg

  def modelPackage(config: ExportConfiguration) = {
    val prelude = if (inputType.isThrift) { Nil } else { config.applicationPackage }
    prelude ++ (pkg.lastOption match {
      case Some("models") => pkg
      case Some("enums") => pkg
      case _ => "models" +: pkg
    })
  }

  def controllerPackage(config: ExportConfiguration) = if (inputType.isThrift) {
    (if (pkg.isEmpty) { List("system") } else { pkg }) :+ "controllers"
  } else {
    config.applicationPackage ++ List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })
  }
}
