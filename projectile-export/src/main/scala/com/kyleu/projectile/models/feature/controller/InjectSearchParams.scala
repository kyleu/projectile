package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

case class InjectSearchParams(config: ExportConfiguration, model: ExportModel) {
  val viewClass = (model.viewHtmlPackage(config) :+ (model.propertyName + "SearchResult")).mkString(".")
  val message = model.pkFields match {
    case Nil => s"""s"${model.title} matched [$$q].""""
    case cols => s"""s"${model.title} [${cols.map(col => "${model." + col.propertyName + "}").mkString(", ")}] matched [$$q].""""
  }

  override def toString = model.key
}
