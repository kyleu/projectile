package com.projectile.models.export.config

import com.projectile.models.export.{ExportEnum, ExportModel, ExportService}
import com.projectile.models.output.file.ScalaFile
import com.projectile.models.output.{CommonImportHelper, OutputPackage}
import com.projectile.models.project.Project
import com.projectile.util.JsonSerializers._

object ExportConfiguration {
  implicit val jsonEncoder: Encoder[ExportConfiguration] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportConfiguration] = deriveDecoder
}

case class ExportConfiguration(
    project: Project,
    enums: Seq[ExportEnum],
    models: Seq[ExportModel],
    services: Seq[ExportService]
) {
  val applicationPackage = project.getPackage(OutputPackage.Application)

  val systemPackage = project.getPackage(OutputPackage.System)

  val resultsPackage = systemPackage ++ project.getPackage(OutputPackage.Results)
  val tagsPackage = systemPackage ++ project.getPackage(OutputPackage.Tags)
  val utilitiesPackage = systemPackage ++ project.getPackage(OutputPackage.Utils)

  def getEnum(k: String) = getEnumOpt(k).getOrElse(throw new IllegalStateException(s"No enum available with name [$k]."))
  def getEnumOpt(k: String) = enums.find(e => e.key == k || e.propertyName == k || e.className == k)

  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = models.find(m => m.key == k || m.propertyName == k || m.className == k)

  def addCommonImport(f: ScalaFile, s: String, additional: String*) = CommonImportHelper.get(this, f, s) match {
    case (p, c) if additional.isEmpty => f.addImport(p, c)
    case (p, c) => f.addImport((p :+ c) ++ additional.init, additional.last)
  }
}
