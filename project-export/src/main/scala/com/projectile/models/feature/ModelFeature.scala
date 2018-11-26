package com.projectile.models.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.OutputFile

sealed abstract class ModelFeature(
    override val value: String,
    val title: String,
    val description: String,
    val dependsOn: Set[ProjectFeature] = Set(ProjectFeature.Core)
) extends StringEnumEntry

object ModelFeature extends StringEnum[ModelFeature] with StringCirceEnum[ModelFeature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends ModelFeature(
    value = "core", title = "Core", description = "Scala case class representing this model"
  )

  case object Json extends ModelFeature(
    value = "json", title = "JSON", description = "Circe Json serializer and deserializer"
  )

  case object Shared extends ModelFeature(
    value = "shared", title = "Shared", description = "Store the file in the shared, rather than server, source directory"
  )

  case object DataModel extends ModelFeature(
    value = "datamodel", title = "Data Model", description = "Adds DataModel transformations to the model", dependsOn = Set(ProjectFeature.DataModel)
  )

  case object ScalaJS extends ModelFeature(
    value = "scalajs", title = "Scala.js", description = "Adds Scala.js annotations to export the model to JavaScript", dependsOn = Set(ProjectFeature.ScalaJS)
  )

  case object Slick extends ModelFeature(
    value = "slick", title = "Slick", description = "Slick JDBC models and bindings", dependsOn = Set(ProjectFeature.Slick)
  )

  case object Doobie extends ModelFeature(
    value = "doobie", title = "Doobie", description = "Doobie JDBC models and queries", dependsOn = Set(ProjectFeature.Doobie)
  )

  case object Service extends ModelFeature(
    value = "service", title = "Service", description = "Generates a service with common CRUD and search methods", dependsOn = Set(ProjectFeature.Service)
  )

  case object Controller extends ModelFeature(
    value = "controller", title = "Controller", description = "Creates a Play Framework controller for this model", dependsOn = Set(ProjectFeature.GraphQL)
  )

  case object GraphQL extends ModelFeature(
    value = "graphql", title = "GraphQL", description = "Creates a Sangria GraphQL schema that includes this model", dependsOn = Set(ProjectFeature.GraphQL)
  )

  case object Thrift extends ModelFeature(
    value = "thrift", title = "Thrift", description = "Generates Thrift definitions for models and services", dependsOn = Set(ProjectFeature.Thrift)
  )

  case object Audit extends ModelFeature(
    value = "audit", title = "Audit", description = "Adds audit logic to service methods", dependsOn = Set(ProjectFeature.Audit)
  )

  override val values = findValues
  val set = values.toSet
}
