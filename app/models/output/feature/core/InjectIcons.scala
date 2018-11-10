package models.output.feature.core

import models.export.config.ExportConfiguration
import models.output.feature.FeatureLogic
import models.output.{ExportHelper, OutputPath}

object InjectIcons extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Icons.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "template"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val startString = "  // Start model icons"
    val endString = "  // End model icons"
    val startIndex = original.indexOf(startString)
    val newContent = config.models.flatMap { m =>
      original.indexOf("val " + m.propertyName + " = ") match {
        case x if x > -1 && x < startIndex => None
        case _ => Some(s"""  val ${m.propertyName} = "fa-${m.icon.getOrElse(randomIcon(m.propertyName))}"""")
      }
    }.sorted.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
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
