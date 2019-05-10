package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectIcons extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Icons.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "template"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val models = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val o = original.mkString("\n")
    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model icons")
    val startIndex = o.indexOf(params.start)
    val endIndex = o.indexOf(params.end)
    val newLines = content(o, startIndex, endIndex, models)
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }

  private[this] def content(o: String, startIndex: Int, endIndex: Int, models: Seq[ExportModel]) = {
    val pkgs = models.flatMap(_.pkg.headOption)
    val sysPkg = if (models.exists(_.pkg.isEmpty)) { Seq("system") } else { Nil }
    val packages = (pkgs ++ sysPkg).distinct.flatMap { pkg =>
      o.indexOf(s"val pkg_$pkg = ") match {
        case x if x > -1 && x < startIndex => None
        case x if x > endIndex => None
        case _ => Some(s"""val pkg_$pkg = "${randomMaterialIcon(pkg)}"""")
      }
    }.sorted

    val mods = models.flatMap { m =>
      o.indexOf("val " + m.propertyName + " = ") match {
        case x if x > -1 && x < startIndex => None
        case x if x > endIndex => None
        case _ => Some(s"""val ${m.propertyName} = "${m.icon.getOrElse(randomMaterialIcon(m.propertyName))}"""")
      }
    }.sorted

    packages ++ mods
  }

  private[this] val materialIcons = IndexedSeq(
    "account_balance", "alarm", "assignment", "book", "build", "cached", "code", "eject", "event",
    "explore", "favorite", "home", "hourglass_empty", "label", "language", "list", "motorcycle", "opacity", "pets",
    "timeline", "note", "videocam", "chat_bubble", "vpn_key", "gesture", "send", "graphic_eq", "devices",
    "gps_fixed", "storage", "space_bar", "cloud_queue", "computer", "watch", "videogame_asset", "brush", "colorize"
  )

  private[this] def randomMaterialIcon(s: String) = materialIcons(Math.abs(s.hashCode) % materialIcons.size)
}
