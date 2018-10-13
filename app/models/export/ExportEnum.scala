package models.export

import models.output.ExportHelper
import util.JsonSerializers._

object ExportEnum {
  implicit val jsonEncoder: Encoder[ExportEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportEnum] = deriveDecoder
}

case class ExportEnum(
    pkg: List[String] = Nil,
    name: String,
    className: String,
    values: Seq[String]
) {
  val valuesWithClassNames = values.map(v => v -> ExportHelper.toClassName(ExportHelper.toIdentifier(v.replaceAllLiterally(".", "_"))))

  val propertyName = ExportHelper.toIdentifier(className)

  val modelPackage = List("models") ++ pkg
  val fullClassName = (modelPackage :+ className).mkString(".")
}
