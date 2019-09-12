package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

case class InjectSearchParams(config: ExportConfiguration, model: ExportModel, fieldName: String) {
  val fieldOpt = if (fieldName == "n/a") { None } else { model.getFieldOpt(fieldName) }
  def field = fieldOpt.getOrElse(throw new IllegalStateException(s"No field matching [$fieldName]"))
  def viewClass = (model.viewHtmlPackage(config) :+ (model.propertyName + "SearchResult")).mkString(".")
  def message = model.pkFields match {
    case Nil => s"""s"${model.title} matched $fieldName [$$q].""""
    case cols => s"""s"${model.title} [${cols.map(col => "${model." + col.propertyName + "}").mkString(", ")}] matched ${field.propertyName} [$$q]""""
  }

  override def toString = model.key
}
