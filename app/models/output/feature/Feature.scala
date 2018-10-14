package models.output.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.export.config.ExportConfiguration
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
    val (files, logs) = logic.export(config, verbose)
    FeatureOutput(feature = this, files = files, logs = logs, duration = System.currentTimeMillis - startMs)
  }
}

object Feature extends StringEnum[Feature] with StringCirceEnum[Feature] {
  trait Logic {
    def export(config: ExportConfiguration, verbose: Boolean): (Seq[OutputFile.Rendered], Seq[String])
  }

  case object Core extends Feature(
    value = "core", title = "Core", tech = "Scala", logic = CoreLogic,
    description = "Scala case classes and Circe Json serializers",
  )

  case object Wiki extends Feature(
    value = "wiki", title = "Wiki", tech = "Markdown", logic = WikiLogic,
    description = "Markdown documentation in Github wiki format"
  )

  override val values = findValues
}
