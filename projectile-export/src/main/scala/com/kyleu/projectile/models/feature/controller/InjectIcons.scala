package com.kyleu.projectile.models.feature.controller

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

    val newLines = if (config.isNewUi) {
      val pkgs = models.flatMap(_.pkg.headOption).distinct.flatMap { pkg =>
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

      pkgs ++ Seq("") ++ mods
    } else {
      models.flatMap { m =>
        o.indexOf("val " + m.propertyName + " = ") match {
          case x if x > -1 && x < startIndex => None
          case x if x > endIndex => None
          case -1 => Some(s"""val ${m.propertyName} = "fa-${m.icon.getOrElse(randomFaIcon(m.propertyName))}"""")
          case _ => None
        }
      }.sorted
    }

    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }

  private[this] val faIcons = IndexedSeq(
    "address-book-o", "anchor", "asterisk", "bar-chart-o", "beer", "bell-o", "bicycle", "birthday-cake", "bookmark-o",
    "bullhorn", "bus", "car", "code", "cog", "cube", "diamond", "envelope-o", "exchange", "eye", "eyedropper",
    "folder-o", "folder-open-o", "frown-o", "futbol-o", "gamepad", "gavel", "gift", "glass", "globe", "graduation-cap",
    "hand-lizard-o", "hand-paper-o", "hand-peace-o", "hand-pointer-o", "hand-rock-o", "hand-scissors-o", "hand-spock-o", "handshake-o",
    "hashtag", "hdd-o", "headphones", "heart", "heart-o", "heartbeat", "history", "home", "hourglass", "hourglass-o", "hourglass-start",
    "i-cursor", "id-badge", "id-card", "id-card-o", "inbox", "industry", "info", "info-circle", "key", "keyboard-o",
    "language", "laptop", "leaf", "lemon-o", "level-down", "level-up", "life-ring", "lightbulb-o", "line-chart", "location-arrow",
    "lock", "low-vision", "magic", "magnet", "male", "map", "map-marker", "map-o", "map-pin", "map-signs",
    "meh-o", "money", "moon-o", "motorcycle", "newspaper-o", "paper-plane-o", "paw", "phone", "photo", "plane", "print", "puzzle-piece"
  )

  private[this] def randomFaIcon(s: String) = faIcons(Math.abs(s.hashCode) % faIcons.size)

  private[this] val materialIcons = IndexedSeq(
    "account_balance", "alarm", "assignment", "book", "build", "cached", "code", "eject", "event",
    "explore", "favorite", "home", "hourglass_empty", "label", "language", "list", "motorcycle", "opacity", "pets",
    "timeline", "note", "videocam", "chat_bubble", "vpn_key", "gesture", "send", "graphic_eq", "devices",
    "gps_fixed", "storage", "space_bar", "cloud_queue", "computer", "watch", "videogame_asset", "brush", "colorize"
  )

  private[this] def randomMaterialIcon(s: String) = materialIcons(Math.abs(s.hashCode) % materialIcons.size)
}
