package models.output.feature.controller.twirl

import models.export.ExportModel
import models.export.config.ExportConfiguration

object TwirlHelper {
  def iconHtml(config: ExportConfiguration, propertyName: String) = {
    s"""<i class="fa @${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"></i>"""
  }

  def routesClass(config: ExportConfiguration, model: ExportModel) = {
    (config.applicationPackage ++ model.routesPackage :+ (model.className + "Controller")).mkString(".")
  }
}
