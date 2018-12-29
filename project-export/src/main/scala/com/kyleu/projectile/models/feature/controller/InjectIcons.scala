package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectIcons extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Icons.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "template"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val models = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model icons")
    val startIndex = original.indexOf(params.start)
    val endIndex = original.indexOf(params.end)

    val newLines = models.flatMap { m =>
      val ret = Some(s"""val ${m.propertyName} = "fa-${m.icon.getOrElse(randomIcon(m.propertyName))}"""")
      original.indexOf("val " + m.propertyName + " = ") match {
        case x if x > -1 && x < startIndex => None
        case x if x > endIndex => None
        case -1 => ret
        case _ => None
      }
    }.sorted

    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }

  private[this] val icons = IndexedSeq(
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

  private[this] def randomIcon(s: String) = icons(Math.abs(s.hashCode) % icons.size)
}
