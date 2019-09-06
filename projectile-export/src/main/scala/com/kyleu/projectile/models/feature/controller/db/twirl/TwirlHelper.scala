package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

object TwirlHelper {
  def iconHtml(config: ExportConfiguration, propertyName: String, provided: Boolean, style: Option[String] = None) = {
    val pkg = if (provided) {
      (config.systemPackage :+ "models" :+ "web" :+ "InternalIcons").mkString(".")
    } else {
      (config.applicationPackage :+ "models" :+ "template" :+ "Icons").mkString(".")
    }
    materialIconHtml(config, propertyName, style, pkg)
  }

  private[this] def materialIconHtml(config: ExportConfiguration, propertyName: String, style: Option[String], pkg: String) = style match {
    case Some(s) => s"""<i class="material-icons small" style="$s">@$pkg.$propertyName</i>"""
    case None => s"""<i class="material-icons small">@$pkg.$propertyName</i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
  }
}
