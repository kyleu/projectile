package com.projectile.models.output.feature.controller.twirl

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration

object TwirlHelper {
  def iconHtml(config: ExportConfiguration, propertyName: String) = {
    s"""<i class="fa @${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"></i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (config.applicationPackage ++ model.routesPackage :+ (model.className + "Controller")).mkString(".")
  }
}
