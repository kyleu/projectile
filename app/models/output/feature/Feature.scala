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
    val logic: Option[Feature.Logic],
    val description: String
) extends StringEnumEntry {
  def export(config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val logs = collection.mutable.ArrayBuffer.empty[OutputLog]

    def info(s: String) = {
      logs += OutputLog(s, System.currentTimeMillis - startMs)
    }
    def debug(s: String) = if (verbose) { info(s) }

    val files = logic.map(_.export(config = config, info = info, debug = debug)).getOrElse(Nil)
    val duration = System.currentTimeMillis - startMs
    debug(s"Feature [$title] produced [${files.length}] files in [${duration}ms]")
    FeatureOutput(feature = this, files = files, logs = logs, duration = duration)
  }
}

object Feature extends StringEnum[Feature] with StringCirceEnum[Feature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends Feature(
    value = "core", title = "Core", tech = "Scala", logic = Some(CoreLogic),
    description = "Scala case classes and Circe Json serializers"
  )

  case object DataModel extends Feature(
    value = "datamodel", title = "Data Model", tech = "Scala", logic = None,
    description = "Defines methods to export models to a common schema"
  )

  case object ScalaJS extends Feature(
    value = "scalajs", title = "Scala.js", tech = "Scala", logic = None,
    description = "Exports models to Scala.js for use from JavaScript"
  )

  case object Audit extends Feature(
    value = "audit", title = "Audit", tech = "Scala", logic = None,
    description = "Logs audits of changed properties for models"
  )

  case object Wiki extends Feature(
    value = "wiki", title = "Wiki", tech = "Markdown", logic = Some(WikiLogic),
    description = "Markdown documentation in Github wiki format"
  )

  override val values = findValues
  val set = values.toSet
}
