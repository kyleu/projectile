package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.project.member.UnionMember
import com.kyleu.projectile.util.JsonSerializers._

object ExportUnion {
  implicit val jsonEncoder: Encoder[ExportUnion] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportUnion] = deriveDecoder
}

case class ExportUnion(
    inputType: InputType.Union,
    pkg: List[String] = Nil,
    key: String,
    className: String,
    types: Seq[ExportField]
) {
  def apply(m: UnionMember) = copy(
    pkg = m.pkg.toList,
    className = m.getOverride("className", className),
    types = types.filterNot(t => m.ignored.apply(t.key))
  )

  val propertyName = ExportHelper.toIdentifier(className)

  def fullClassPath(config: ExportConfiguration) = (modelPackage(config) :+ className).mkString(".")

  def graphqlPackage(config: ExportConfiguration) = if (inputType.isThrift) {
    pkg
  } else {
    config.applicationPackage ++ List("models", "graphql") ++ pkg
  }

  def modelPackage(config: ExportConfiguration) = {
    val prelude = if (inputType.isThrift) { Nil } else { config.applicationPackage }
    prelude ++ (pkg.lastOption match {
      case Some("models") => pkg
      case Some("enums") => pkg
      case _ => "models" +: pkg
    })
  }
}
