package com.projectile.models.feature.controller

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.controller.twirl.TwirlHelper
import com.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectExploreMenu extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "menu.scala.html") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "views" :+ "admin" :+ "layout"

  private[this] def modelsFor(config: ExportConfiguration) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller))
    val roots = filtered.filter(_.pkg.isEmpty).sortBy(_.title)
    val pkgGroups = filtered.filterNot(_.pkg.isEmpty).groupBy(_.pkg.head).mapValues(_.sortBy(_.title)).toSeq.sortBy(_._1)
    roots -> pkgGroups
  }

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val (roots, pkgGroups) = modelsFor(config)

    val newContent = (roots ++ pkgGroups.flatMap(_._2)).sortBy(_.title).map { model =>
      s"""  <li><a href="@${TwirlHelper.routesClass(config, model)}.list()">${TwirlHelper.iconHtml(config, model.propertyName)} ${model.title}</a></li>"""
    }.mkString("\n")
    ExportHelper.replaceBetween(
      filename = filename, original = original, start = "  <!-- Start model list routes -->", end = "  <!-- End model list routes -->", newContent = newContent
    )
  }
}
