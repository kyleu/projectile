package models.export

import models.output.ExportHelper
import models.output.feature.EnumFeature
import models.project.member.EnumMember
import models.project.member.EnumMember.InputType
import util.JsonSerializers._

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

  val valuesWithClassNames = values.map(v => v -> ExportHelper.toClassName(ExportHelper.toIdentifier(v.replaceAllLiterally(".", "_"))))

  val propertyName = ExportHelper.toIdentifier(className)

  val modelPackage = List("models") ++ pkg
  val slickPackage = List("models", "table") ++ pkg
  val doobiePackage = List("models", "doobie") ++ pkg

  val controllerPackage = List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })
}
