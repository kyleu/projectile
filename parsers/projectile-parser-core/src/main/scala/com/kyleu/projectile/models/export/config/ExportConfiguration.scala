package com.kyleu.projectile.models.export.config

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService, ExportUnion}
import com.kyleu.projectile.models.output.file.{OutputFile, ScalaFile}
import com.kyleu.projectile.models.output.{CommonImportHelper, OutputPackage}
import com.kyleu.projectile.models.project.Project

case class ExportConfiguration(
    project: Project,
    enums: Seq[ExportEnum] = Nil,
    models: Seq[ExportModel] = Nil,
    unions: Seq[ExportUnion] = Nil,
    services: Seq[ExportService] = Nil,
    additional: Seq[OutputFile] = Nil
) {
  val applicationPackage = project.getPackage(OutputPackage.Application)
  val systemPackage = project.getPackage(OutputPackage.System)

  val viewPackage = applicationPackage ++ project.getPackage(OutputPackage.TwirlViews)
  val systemViewPackage = systemPackage ++ project.getPackage(OutputPackage.TwirlViews)
  val componentViewPackage = systemPackage ++ Seq("components") ++ project.getPackage(OutputPackage.TwirlViews)

  val resultsPackage = systemPackage ++ project.getPackage(OutputPackage.Results)
  val tagsPackage = systemPackage ++ project.getPackage(OutputPackage.Tags)
  val utilitiesPackage = systemPackage ++ project.getPackage(OutputPackage.Utils)

  val isNewUi = project.flags("components")

  def withEnums(e: ExportEnum*) = this.copy(enums = enums ++ e)
  def getEnumOpt(k: String) = enums.find(_.key == k)
  def getEnum(k: String, ctx: String) = getEnumOpt(k).getOrElse {
    throw new IllegalStateException(s"No enum for [$ctx] available with name [$k] among candidates [${enums.map(_.key).sorted.mkString(", ")}]")
  }

  def withModels(m: ExportModel*) = this.copy(models = models ++ m)
  def getModelOpt(k: String) = models.find(_.key == k)
  def getModel(k: String, ctx: String) = getModelOpt(k).getOrElse {
    throw new IllegalStateException(s"No model for [$ctx] available with name [$k] among candidates [${models.map(_.key).sorted.mkString(", ")}]")
  }

  def withUnions(u: ExportUnion*) = this.copy(unions = unions ++ u)
  def getUnionOpt(k: String) = unions.find(_.key == k)
  def getUnion(k: String, ctx: String) = getUnionOpt(k).getOrElse {
    throw new IllegalStateException(s"No union for [$ctx] available with name [$k] among candidates [${unions.map(_.key).sorted.mkString(", ")}]")
  }

  def withServices(s: ExportService*) = this.copy(services = services ++ s)
  def getServiceOpt(k: String) = services.find(_.key == k)
  def getService(k: String, ctx: String) = getServiceOpt(k).getOrElse {
    throw new IllegalStateException(s"No service for [$ctx] available with name [$k] among candidates [${services.map(_.key).sorted.mkString(", ")}]")
  }

  def withAdditional(a: OutputFile*) = this.copy(additional = additional ++ a)
  def getAdditionalOpt(k: String) = additional.find(_.key == k)
  def getAdditional(k: String, ctx: String) = getAdditionalOpt(k).getOrElse {
    throw new IllegalStateException(s"No additional for [$ctx] available with name [$k] among candidates [${additional.map(_.key).sorted.mkString(", ")}]")
  }

  def addCommonImport(f: ScalaFile, s: String, additional: String*) = CommonImportHelper.get(this, s) match {
    case (p, c) if additional.isEmpty => f.addImport(p, c)
    case (p, c) => f.addImport((p :+ c) ++ additional.init, additional.lastOption.getOrElse(throw new IllegalStateException()))
  }

  def merge(o: ExportConfiguration) = ExportConfiguration(
    project = project,
    enums = enums ++ o.enums,
    models = models ++ o.models,
    unions = unions ++ o.unions,
    services = services ++ o.services,
    additional = additional ++ o.additional
  )
}
