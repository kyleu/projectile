package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectRoutes extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "routes") {
  override def applies(config: ExportConfiguration) = config.models.exists(m => m.features(ModelFeature.Controller) && m.inputType.isDatabase)
  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val packages = filtered.flatMap(_.pkg.headOption).distinct

    def routeFor(pkg: String) = {
      val detailUrl = s"/admin/$pkg"
      val detailWs = (0 until (39 - detailUrl.length)).map(_ => " ").mkString
      s"->          $detailUrl $detailWs $pkg.Routes"
    }

    val newLines = packages.sorted.map(routeFor)

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Routes, key = "model route files")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
