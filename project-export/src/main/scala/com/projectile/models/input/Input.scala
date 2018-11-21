package com.projectile.models.input

import com.projectile.models.export.{ExportEnum, ExportModel}

abstract class Input() extends Ordered[Input] {
  def template: InputTemplate
  def key: String
  def title: String
  def description: String

  def exportEnum(key: String): ExportEnum
  def exportEnums: Seq[ExportEnum]

  def exportModel(key: String): ExportModel
  def exportModels: Seq[ExportModel]

  override def compare(that: Input) = title.compare(that.title)
}
