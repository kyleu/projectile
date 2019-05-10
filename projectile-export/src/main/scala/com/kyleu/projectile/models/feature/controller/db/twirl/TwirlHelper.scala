package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

object TwirlHelper {
  def iconHtml(config: ExportConfiguration, propertyName: String, style: Option[String] = None) = {
    val pkg = (config.applicationPackage :+ "models" :+ "template").mkString(".")
    materialIconHtml(config, propertyName, style, pkg)
  }

  def materialIconHtml(config: ExportConfiguration, propertyName: String, style: Option[String], pkg: String) = style match {
    case Some(s) => s"""<i class="material-icons small" style="$s">@$pkg.Icons.$propertyName</i>"""
    case None => s"""<i class="material-icons small">@$pkg.Icons.$propertyName</i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
  }
}
