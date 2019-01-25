package com.kyleu.projectile.models.input

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService}

abstract class Input() extends Ordered[Input] {
  def template: InputTemplate
  def key: String
  def description: String

  def exportEnum(key: String): ExportEnum
  def exportEnums: Seq[ExportEnum]

  def exportModel(k: String): ExportModel
  def exportModels: Seq[ExportModel]

  def exportService(k: String): ExportService
  def exportServices: Seq[ExportService]

  override def compare(that: Input) = key.compare(that.key)

  def getEnumOpt(k: String) = exportEnums.find(_.key == k)
  def getEnum(k: String) = getEnumOpt(k).getOrElse(throw new IllegalStateException(s"No enum available with key [$k]"))

  def getModelOpt(k: String) = exportModels.find(_.key == k)
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with key [$k]"))

  def getServiceOpt(k: String) = exportServices.find(_.key == k)
  def getService(k: String) = getServiceOpt(k).getOrElse(throw new IllegalStateException(s"No model available with key [$k]"))

  override def toString = s"$key - [${exportEnums.size}] enums, [${exportModels.size}] models, and [${exportServices.size}] services"

  lazy val hash = {
    val e = exportEnums.map(_.hashCode).sum
    val m = exportModels.map(_.hashCode).sum
    val s = exportServices.map(_.hashCode).sum
    e + m + s
  }
}
