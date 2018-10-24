package models.export.config

import models.export.{ExportEnum, ExportModel}
import models.output.OutputPackage
import models.project.Project
import util.JsonSerializers._

object ExportConfiguration {
  implicit val jsonEncoder: Encoder[ExportConfiguration] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportConfiguration] = deriveDecoder
}

case class ExportConfiguration(
    project: Project,
    enums: Seq[ExportEnum],
    models: Seq[ExportModel]
) {

  val applicationPackage = project.getPackage(OutputPackage.Application)

  val systemPackage = project.getPackage(OutputPackage.System)

  val resultsPackage = systemPackage ++ project.getPackage(OutputPackage.Results)
  val tagsPackage = systemPackage ++ project.getPackage(OutputPackage.Tags)
  val utilitiesPackage = systemPackage ++ project.getPackage(OutputPackage.Utils)

  def getEnum(k: String) = getEnumOpt(k).getOrElse(throw new IllegalStateException(s"No enum available with name [$k]."))
  def getEnumOpt(k: String) = enums.find(e => e.name == k || e.propertyName == k || e.className == k)

  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = models.find(m => m.name == k || m.propertyName == k || m.className == k)
}
