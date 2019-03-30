package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration

object TwirlHelper {
  def faIconHtml(config: ExportConfiguration, propertyName: String) = {
    s"""<i class="fa @${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"></i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
  }
}
