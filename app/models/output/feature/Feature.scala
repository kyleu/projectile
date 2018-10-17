package models.output.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.export.config.ExportConfiguration
import models.output.OutputLog
import models.output.feature.core.CoreLogic
import models.output.feature.wiki.WikiLogic
import models.output.file.OutputFile

sealed abstract class Feature(
    override val value: String,
    val title: String,
    val tech: String,
    val logic: Feature.Logic,
    val description: String
) extends StringEnumEntry {
  def export(config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val logs = collection.mutable.ArrayBuffer.empty[OutputLog]

    def info(s: String) = {
      logs += OutputLog(s, System.currentTimeMillis - startMs)
    }
    def debug(s: String) = if (verbose) { info(s) }

    val files = logic.export(config = config, info = info, debug = debug)
    val duration = System.currentTimeMillis - startMs
    debug(s"Feature [$title] completed in [${duration}ms]")
    FeatureOutput(feature = this, files = files, logs = logs, duration = duration)
  }
}

object Feature extends StringEnum[Feature] with StringCirceEnum[Feature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends Feature(
    value = "core", title = "Core", tech = "Scala", logic = CoreLogic,
    description = "Scala case classes and Circe Json serializers"
  )

  case object Wiki extends Feature(
    value = "wiki", title = "Wiki", tech = "Markdown", logic = WikiLogic,
    description = "Markdown documentation in Github wiki format"
  )

  override val values = findValues
  val set = values.toSet
}
