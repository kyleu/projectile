package com.kyleu.projectile.models.feature

import com.kyleu.projectile.models.input.InputTemplate
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.OutputPath._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ProjectFeature(
    override val value: String,
    val title: String,
    val tech: String,
    val paths: Set[OutputPath],
    val appliesTo: Set[InputTemplate],
    val description: String
) extends StringEnumEntry

object ProjectFeature extends StringEnum[ProjectFeature] with StringCirceEnum[ProjectFeature] {
  case object Core extends ProjectFeature(
    value = "core", title = "Core", tech = "Scala", paths = Set(Root, SharedSource, ServerSource),
    appliesTo = InputTemplate.all, description = "Scala case classes and Circe Json serializers"
  )

  case object DataModel extends ProjectFeature(
    value = "datamodel", title = "Data Model", tech = "Scala", paths = Set(SharedSource, ServerSource),
    appliesTo = InputTemplate.all, description = "Defines methods to export models to a common schema, and creates search result response classes"
  )

  case object ScalaJS extends ProjectFeature(
    value = "scalajs", title = "Scala.js", tech = "Scala", paths = Set(SharedSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Exports models to Scala.js for use from JavaScript"
  )

  case object Slick extends ProjectFeature(
    value = "slick", title = "Slick", tech = "Scala", paths = Set(ServerSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Slick JDBC classes and supporting queries"
  )

  case object Doobie extends ProjectFeature(
    value = "doobie", title = "Doobie", tech = "Scala", paths = Set(ServerSource, ServerTest),
    appliesTo = Set(InputTemplate.Postgres), description = "Doobie JDBC classes and supporting queries"
  )

  case object Service extends ProjectFeature(
    value = "service", title = "Service", tech = "Scala", paths = Set(ServerSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Custom service and supporting queries for common operations"
  )

  case object GraphQL extends ProjectFeature(
    value = "graphql", title = "GraphQL", tech = "Scala", paths = Set(GraphQLOutput),
    appliesTo = InputTemplate.all, description = "Sangria bindings for an exported GraphQL schema"
  )

  case object Controller extends ProjectFeature(
    value = "controller", title = "Controller", tech = "Scala", paths = Set(ServerSource, ServerResource),
    appliesTo = InputTemplate.all, description = "Play Framework controller for common operations"
  )

  case object Audit extends ProjectFeature(
    value = "audit", title = "Audit", tech = "Scala", paths = Set(ServerSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Logs audits of changed properties for models"
  )

  case object Notes extends ProjectFeature(
    value = "notes", title = "Notes", tech = "Scala", paths = Set(ServerSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Allows the creation and viewing of notes for any model instance"
  )

  case object Tests extends ProjectFeature(
    value = "tests", title = "Tests", tech = "Scala", paths = Set(ServerSource),
    appliesTo = Set(InputTemplate.Postgres), description = "Scalatest unit tests for selected features"
  )

  case object Thrift extends ProjectFeature(
    value = "thrift", title = "Thrift", tech = "Thrift", paths = Set(ThriftOutput),
    appliesTo = Set(InputTemplate.Postgres), description = "Thrift definitions for exported models and services"
  )

  case object OpenAPI extends ProjectFeature(
    value = "openapi", title = "OpenAPI", tech = "JSON", paths = Set(OpenAPIJson),
    appliesTo = Set(InputTemplate.Postgres), description = "OpenAPI/Swagger documentation of generated methods"
  )

  case object Wiki extends ProjectFeature(
    value = "wiki", title = "Wiki", tech = "Markdown", paths = Set(WikiMarkdown),
    appliesTo = Set(InputTemplate.Postgres), description = "Markdown documentation in GitHub wiki format"
  )

  override val values = findValues
  val set = values.toSet
}
