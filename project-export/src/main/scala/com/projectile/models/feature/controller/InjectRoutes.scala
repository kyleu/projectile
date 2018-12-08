package com.projectile.models.feature.controller

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectRoutes extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "routes") {
  val startString = "# Start model route files"
  val endString = "# End model route files"

  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val packages = filtered.flatMap(_.pkg.headOption).distinct

    def routeFor(pkg: String) = {
      val detailUrl = s"/admin/$pkg"
      val detailWs = (0 until (39 - detailUrl.length)).map(_ => " ").mkString
      s"->          $detailUrl $detailWs $pkg.Routes"
    }

    val newContent = packages.sorted.map(routeFor).mkString("\n")
    if (newContent.trim.isEmpty) {
      original
    } else {
      ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
    }
  }
}
