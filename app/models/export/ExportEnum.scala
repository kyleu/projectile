package models.export

import models.output.ExportHelper
import models.output.feature.Feature
import models.project.member.ProjectMember
import models.project.member.ProjectMember.InputType
import util.JsonSerializers._

object ExportEnum {
  implicit val jsonEncoder: Encoder[ExportEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportEnum] = deriveDecoder
}

case class ExportEnum(
    inputType: InputType,
    pkg: List[String] = Nil,
    name: String,
    className: String,
    values: Seq[String],
    features: Set[Feature] = Set.empty
) {

  def apply(m: ProjectMember) = copy(
    pkg = m.outputPackage.toList,
    className = ExportHelper.toClassName(m.outputKey),
    values = values.filterNot(m.ignored.contains),
    features = m.features
  )

  val valuesWithClassNames = values.map(v => v -> ExportHelper.toClassName(ExportHelper.toIdentifier(v.replaceAllLiterally(".", "_"))))

  val propertyName = ExportHelper.toIdentifier(className)

  val modelPackage = List("models") ++ pkg
  val tablePackage = List("models", "table") ++ pkg

  val fullClassName = (modelPackage :+ className).mkString(".")
}
