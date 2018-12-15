package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.feature.controller.db.RoutesFiles
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object InjectSystemRoutes extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "system.routes") {
  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val systemModels = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase).filter(_.pkg.isEmpty)
    val newContent = systemModels.flatMap(m => RoutesFiles.routesContentFor(config, m)).mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = "# Start model routes", end = "# End model routes", newContent = newContent)
  }
}
