package com.kyleu.projectile.models.export.config

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService, ExportUnion}
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{CommonImportHelper, OutputPackage}
import com.kyleu.projectile.models.project.Project
import com.kyleu.projectile.util.JsonSerializers._

object ExportConfiguration {
  implicit val jsonEncoder: Encoder[ExportConfiguration] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportConfiguration] = deriveDecoder
}

case class ExportConfiguration(
    project: Project,
    enums: Seq[ExportEnum],
    models: Seq[ExportModel],
    unions: Seq[ExportUnion],
    services: Seq[ExportService]
) {
  val applicationPackage = project.getPackage(OutputPackage.Application)
  val systemPackage = project.getPackage(OutputPackage.System)

  val viewPackage = applicationPackage ++ project.getPackage(OutputPackage.TwirlViews)
  val systemViewPackage = systemPackage ++ project.getPackage(OutputPackage.TwirlViews)

  val resultsPackage = systemPackage ++ project.getPackage(OutputPackage.Results)
  val tagsPackage = systemPackage ++ project.getPackage(OutputPackage.Tags)
  val utilitiesPackage = systemPackage ++ project.getPackage(OutputPackage.Utils)

  def getEnumOpt(k: String) = enums.find(_.key == k)
  def getEnum(k: String, ctx: String) = getEnumOpt(k).getOrElse {
    throw new IllegalStateException(s"No enum for [$ctx] available with name [$k] among candidates [${enums.map(_.key).sorted.mkString(", ")}]")
  }

  def getModelOpt(k: String) = models.find(_.key == k)
  def getModel(k: String, ctx: String) = getModelOpt(k).getOrElse {
    throw new IllegalStateException(s"No model for [$ctx] available with name [$k] among candidates [${models.map(_.key).sorted.mkString(", ")}]")
  }

  def getUnionOpt(k: String) = unions.find(_.key == k)
  def getUnion(k: String, ctx: String) = getUnionOpt(k).getOrElse {
    throw new IllegalStateException(s"No union for [$ctx] available with name [$k] among candidates [${unions.map(_.key).sorted.mkString(", ")}]")
  }

  def getServiceOpt(k: String) = services.find(_.key == k)
  def getService(k: String, ctx: String) = getServiceOpt(k).getOrElse {
    throw new IllegalStateException(s"No service for [$ctx] available with name [$k] among candidates [${services.map(_.key).sorted.mkString(", ")}]")
  }

  def addCommonImport(f: ScalaFile, s: String, additional: String*) = CommonImportHelper.get(this, s) match {
    case (p, c) if additional.isEmpty => f.addImport(p, c)
    case (p, c) => f.addImport((p :+ c) ++ additional.init, additional.lastOption.getOrElse(throw new IllegalStateException()))
  }
}
