package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

object TwirlHelper {
  def iconHtml(config: ExportConfiguration, propertyName: String, style: Option[String] = None) = if (config.isNewUi) {
    materialIconHtml(config, propertyName, style)
  } else {
    faIconHtml(config, propertyName, style)
  }

  def faIconHtml(config: ExportConfiguration, propertyName: String, style: Option[String] = None) = style match {
    case Some(s) => s"""<i style="$s" class="fa @${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"></i>"""
    case None => s"""<i class="fa @${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"></i>"""
  }

  def materialIconHtml(config: ExportConfiguration, propertyName: String, style: Option[String] = None) = style match {
    case Some(s) => s"""<i class="material-icons" style="$s">@${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName</i>"""
    case None => s"""<i class="material-icons">@${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName</i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
  }
}
