package models.output.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.export.config.ExportConfiguration
import models.output.{OutputLog, OutputPath}
import models.output.feature.core.CoreLogic
import models.output.feature.datamodel.DataModelLogic
import models.output.feature.wiki.WikiLogic
import models.output.file.OutputFile
import models.output.OutputPath._
import models.output.feature.doobie.DoobieLogic
import models.output.feature.graphql.GraphQLLogic
import models.output.feature.service.ServiceLogic
import models.output.feature.slick.SlickLogic

sealed abstract class Feature(
    override val value: String,
    val title: String,
    val tech: String,
    val logic: Option[Feature.Logic],
    val paths: Set[OutputPath],
    val description: String
) extends StringEnumEntry {
  def appliesToModel = false
  def appliesToEnum = false

  def export(config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val logs = collection.mutable.ArrayBuffer.empty[OutputLog]

    def info(s: String) = {
      logs += OutputLog(s, System.currentTimeMillis - startMs)
    }
    def debug(s: String) = if (verbose) { info(s) }

    val files = logic.map(_.export(config = config, info = info, debug = debug)).getOrElse(Nil)
    val duration = System.currentTimeMillis - startMs
    info(s"Feature [$title] produced [${files.length}] files in [${duration}ms]")
    FeatureOutput(feature = this, files = files, logs = logs, duration = duration)
  }
}

object Feature extends StringEnum[Feature] with StringCirceEnum[Feature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends Feature(
    value = "core", title = "Core", tech = "Scala", logic = Some(CoreLogic), paths = Set(Root),
    description = "Scala case classes and Circe Json serializers"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object DataModel extends Feature(
    value = "datamodel", title = "Data Model", tech = "Scala", logic = Some(DataModelLogic), paths = Set(SharedSource, ServerSource),
    description = "Defines methods to export models to a common schema, and creates search result response classes"
  ) {
    override val appliesToModel = true
  }

  case object ScalaJS extends Feature(
    value = "scalajs", title = "Scala.js", tech = "Scala", logic = None, paths = Set(SharedSource),
    description = "Exports models to Scala.js for use from JavaScript"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object Audit extends Feature(
    value = "audit", title = "Audit", tech = "Scala", logic = None, paths = Set(ServerSource),
    description = "Logs audits of changed properties for models"
  ) {
    override val appliesToModel = true
  }

  case object GraphQL extends Feature(
    value = "graphql", title = "GraphQL", tech = "Scala", logic = Some(GraphQLLogic), paths = Set(ServerSource),
    description = "Sangria bindings for an exported GraphQL schema"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object Service extends Feature(
    value = "service", title = "Service", tech = "Scala", logic = Some(ServiceLogic), paths = Set(ServerSource),
    description = "Custom service and supporting queries for common operations"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object Slick extends Feature(
    value = "slick", title = "Slick", tech = "Scala", logic = Some(SlickLogic), paths = Set(ServerSource),
    description = "Slick JDBC classes and supporting queries"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object Doobie extends Feature(
    value = "doobie", title = "Doobie", tech = "Scala", logic = Some(DoobieLogic), paths = Set(ServerSource),
    description = "Doobie JDBC classes and supporting queries"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  case object Wiki extends Feature(
    value = "wiki", title = "Wiki", tech = "Markdown", logic = Some(WikiLogic), paths = Set(WikiMarkdown),
    description = "Markdown documentation in Github wiki format"
  ) {
    override val appliesToEnum = true
    override val appliesToModel = true
  }

  override val values = findValues
  val set = values.toSet
}
