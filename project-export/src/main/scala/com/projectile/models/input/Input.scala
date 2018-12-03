package com.projectile.models.input

import com.projectile.models.export.{ExportEnum, ExportModel, ExportService}

abstract class Input() extends Ordered[Input] {
  def template: InputTemplate
  def key: String
  def title: String
  def description: String

  def exportEnum(key: String): ExportEnum
  def exportEnums: Seq[ExportEnum]

  def exportModel(k: String): ExportModel
  def exportModels: Seq[ExportModel]

  def exportService(k: String): ExportService
  def exportServices: Seq[ExportService]

  override def compare(that: Input) = title.compare(that.title)

  def getEnumOpt(k: String) = exportEnums.find(_.key == k)
  def getEnum(k: String) = getEnumOpt(k).getOrElse(throw new IllegalStateException(s"No enum available with key [$k]"))

  def getModelOpt(k: String) = exportModels.find(_.key == k)
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with key [$k]"))
}
