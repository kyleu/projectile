package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.controller.db.twirl.TwirlHelper
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectComponentMenu extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ComponentMenu.scala") {
  override def applies(config: ExportConfiguration) = config.models.exists(_.features(ModelFeature.Controller))
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "template"

  private[this] def modelsFor(config: ExportConfiguration) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val roots = filtered.filter(_.pkg.isEmpty).sortBy(_.title)
    val pkgGroups = filtered.filterNot(_.pkg.isEmpty).groupBy(_.pkg.headOption.getOrElse(
      throw new IllegalStateException()
    )).map(x => x._1 -> x._2.sortBy(_.title)).toSeq.sortBy(_._1)
    roots -> pkgGroups
  }

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val (roots, pkgGroups) = modelsFor(config)

    val rootGroup = if (roots.isEmpty) { Nil } else { Seq("system" -> roots) }
    val groups = (rootGroup ++ pkgGroups).sortBy(_._1)

    val newLines = groups.flatMap { pkgGroup =>
      pkgGroup._2.toList match {
        case Nil => None
        // case model :: Nil => Some(model.title -> Seq(s"""// ${model.title}"""))
        case models =>
          val section = ExportHelper.toClassName(pkgGroup._1)

          val sectionIcon = s"${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.pkg_${pkgGroup._1}"
          val h = s"""Seq(NavMenu(key = "${pkgGroup._1}", title = "$section", url = None, icon = Some($sectionIcon), children = Seq("""
          // val t = s"""), flatSection = true)$comma"""
          val t = s"""))) ++"""

          val memberLines = h +: models.map { model =>
            val lastMember = models.lastOption.contains(model)
            val commaMember = if (lastMember) { "" } else { "," }
            val url = s"${TwirlHelper.routesClass(config, model)}.list().url"
            val icon = s"${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.${model.propertyName}"
            s"""  NavMenu(key = "${model.key}", title = "${model.plural}", url = Some($url), icon = Some($icon))$commaMember"""
          } :+ t

          Some(section -> memberLines)
      }
    }

    val lines = newLines.sortBy(_._1).flatMap(_._2)

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "component menu items")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = lines, project = config.project.key)
  }
}
