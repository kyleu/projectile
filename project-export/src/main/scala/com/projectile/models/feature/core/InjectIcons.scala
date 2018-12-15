package com.projectile.models.feature.core

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.FeatureLogic
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectIcons extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Icons.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "template"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val dbModels = config.models.filter(_.inputType.isDatabase)
    val postDb = if (dbModels.isEmpty) { original } else { dbLogic(dbModels, original) }

    val thriftModels = config.models.filter(_.inputType.isThrift)
    if (thriftModels.isEmpty) { postDb } else { thriftLogic(thriftModels, postDb) }
  }

  private[this] def dbLogic(models: Seq[ExportModel], original: String) = {
    val startString = "  // Start model icons"
    val endString = "  // End model icons"
    val startIndex = original.indexOf(startString)
    val newContent = models.flatMap { m =>
      original.indexOf("val " + m.propertyName + " = ") match {
        case x if x > -1 && x < startIndex => None
        case _ => Some(s"""  val ${m.propertyName} = "fa-${m.icon.getOrElse(randomIcon(m.propertyName))}"""")
      }
    }.sorted.mkString("\n")

    if (newContent.trim.isEmpty) {
      original
    } else {
      ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
    }
  }

  private[this] def thriftLogic(models: Seq[ExportModel], original: String) = {
    val startString = "  // Start thrift icons"
    val endString = "  // End thrift icons"
    val startIndex = original.indexOf(startString)
    val newContent = models.flatMap { m =>
      original.indexOf("val " + m.propertyName + " = ") match {
        case x if x > -1 && x < startIndex => None
        case _ => Some(s"""  val ${m.propertyName} = "fa-${m.icon.getOrElse(randomIcon(m.propertyName))}"""")
      }
    }.sorted.mkString("\n")

    if (newContent.trim.isEmpty) {
      original
    } else {
      ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
    }
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
