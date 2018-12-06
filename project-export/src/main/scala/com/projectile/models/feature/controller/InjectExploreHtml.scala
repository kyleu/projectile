package com.projectile.models.feature.controller

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.controller.db.twirl.TwirlHelper
import com.projectile.models.output.{ExportHelper, OutputPath}
import com.projectile.models.feature.{FeatureLogic, ModelFeature}

object InjectExploreHtml extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "explore.scala.html") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "views" :+ "admin" :+ "explore"

  private[this] val (sStart, sEnd) = "    <!-- Start model list routes -->" -> "    <!-- End model list routes -->"

  private[this] def modelsFor(config: ExportConfiguration) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val roots = filtered.filter(_.pkg.isEmpty).sortBy(_.title)
    val pkgGroups = filtered.filterNot(_.pkg.isEmpty).groupBy(_.pkg.head).mapValues(_.sortBy(_.title)).toSeq.sortBy(_._1)
    roots -> pkgGroups
  }

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val (roots, pkgGroups) = modelsFor(config)

    val newContent = (roots ++ pkgGroups.flatMap(_._2)).sortBy(_.title).map { model =>
      val routesClass = TwirlHelper.routesClass(config, model)
      s"""    <li class="collection-item">
        |      <a class="theme-text" href="@$routesClass.list()">${TwirlHelper.iconHtml(config, model.propertyName)} ${model.plural}</a>
        |      <div><em>Manage the ${model.plural} of the system.</em></div>
        |    </li>""".stripMargin
    }.mkString("\n")

    ExportHelper.replaceBetween(filename = filename, original = original, start = sStart, end = sEnd, newContent = newContent)
  }
}
