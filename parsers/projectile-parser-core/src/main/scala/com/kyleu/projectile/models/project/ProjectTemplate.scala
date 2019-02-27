package com.kyleu.projectile.models.project

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

sealed abstract class ProjectTemplate(
    override val value: String,
    val title: String,
    val description: String,
    val icon: String
) extends StringEnumEntry {
  def path(p: OutputPath) = p match {
    case OutputPath.Root => "."
    case OutputPath.GraphQLOutput => "src/main/resources/graphql"
    case OutputPath.OpenAPIJson => "src/main/resources/openapi"
    case OutputPath.ServerResource => "src/main/resources"
    case OutputPath.ServerSource => "src/main/scala"
    case OutputPath.ServerTest => "src/test/scala"
    case OutputPath.SharedSource => "src/main/scala"
    case OutputPath.SharedTest => "src/test/scala"
    case OutputPath.ThriftOutput => "src/main/thrift"
    case OutputPath.WikiMarkdown => "wiki"
  }
}

object ProjectTemplate extends StringEnum[ProjectTemplate] with StringCirceEnum[ProjectTemplate] {
  case object ScalaLibrary extends ProjectTemplate(
    value = "scala-library",
    title = "Scala Library",
    description = "A simple Scala library, built with sbt, that depends on Circe and Enumeratum",
    icon = Icons.library
  )

  case object Play extends ProjectTemplate(
    value = "play",
    title = "Play Framework",
    description = "A Play Framework application with some useful defaults and helper classes",
    icon = Icons.library
  ) {
    override def path(p: OutputPath) = p match {
      case OutputPath.GraphQLOutput => "conf/graphql"
      case OutputPath.OpenAPIJson => "conf/openapi"
      case OutputPath.ServerResource => "conf"
      case OutputPath.ServerSource => "app"
      case OutputPath.ServerTest => "test"
      case OutputPath.SharedSource => "app"
      case OutputPath.SharedTest => "test"
      case _ => super.path(p)
    }
  }

  case object ScalaJS extends ProjectTemplate(
    value = "scalajs",
    title = "Scala.js",
    description = "A full web application with shared models and a Scala.js client project",
    icon = Icons.web
  ) {
    override def path(p: OutputPath) = p match {
      case OutputPath.GraphQLOutput => "conf/graphql"
      case OutputPath.OpenAPIJson => "conf/openapi"
      case OutputPath.ServerResource => "conf"
      case OutputPath.ServerSource => "app"
      case OutputPath.ServerTest => "test"
      case OutputPath.SharedSource => "shared/src/main/scala"
      case OutputPath.SharedTest => "shared/src/test/scala"
      case OutputPath.ThriftOutput => "doc/src/main/thrift"
      case OutputPath.WikiMarkdown => "doc/src/main/paradox"
      case _ => super.path(p)
    }
  }

  case object Custom extends ProjectTemplate(
    value = "custom",
    title = "Custom",
    description = "A custom template allowing you to specify paths and options manually",
    icon = Icons.project
  )

  override val values = findValues
}
