package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.feature.controller.db.RoutesFiles
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectSystemRoutes extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "system.routes") {
  override def applies(config: ExportConfiguration) = config.models.exists(m => m.features(ModelFeature.Controller) && m.pkg.isEmpty)
  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val systemModels = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase).filter(_.pkg.isEmpty)
    val newLines = systemModels.flatMap(m => RoutesFiles.routesContentFor(config, m))

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Routes, key = "model routes")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
