package models.project

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.output.OutputPath
import models.output.feature.Feature
import models.output.feature.Feature._
import models.template.Icons

sealed abstract class ProjectTemplate(
    override val value: String,
    val title: String,
    val description: String,
    val repo: String,
    val icon: String,
    val features: Set[Feature]
) extends StringEnumEntry {
  def path(p: OutputPath) = p match {
    case OutputPath.Root => "."
    case OutputPath.ServerSource => "src/main/scala"
    case OutputPath.ServerTest => "src/test/scala"
    case OutputPath.SharedSource => "src/main/scala"
    case OutputPath.SharedTest => "src/test/scala"
    case OutputPath.WikiMarkdown => "wiki"
  }
}

object ProjectTemplate extends StringEnum[ProjectTemplate] with StringCirceEnum[ProjectTemplate] {
  case object ScalaLibrary extends ProjectTemplate(
    value = "scala-library",
    title = "Scala Library",
    description = "A simple Scala library, built with sbt, that depends on Circe and Enumeratum",
    repo = "https://github.com/KyleU/projectile-template-scala-library.git",
    icon = Icons.library,
    features = Set(Core, DataModel, Wiki)
  )

  case object Play extends ProjectTemplate(
    value = "play",
    title = "Play Framework",
    description = "A simple Scala Play Framework application with some useful defaults and helper classes",
    repo = "https://github.com/KyleU/projectile-template-play.git",
    icon = Icons.library,
    features = Set(Core, DataModel, Wiki)
  ) {
    override def path(p: OutputPath) = p match {
      case OutputPath.ServerSource => "app"
      case OutputPath.ServerTest => "test"
      case OutputPath.SharedSource => "app"
      case OutputPath.SharedTest => "test"
      case _ => super.path(p)
    }
  }

  case object Boilerplay extends ProjectTemplate(
    value = "boilerplay",
    title = "Boilerplay",
    description = "Constantly updated, Boilerplay is a starter web application with loads of features",
    repo = "https://github.com/KyleU/boilerplay.git",
    icon = Icons.web,
    features = Set(Core, DataModel, ScalaJS, Audit, Wiki)
  ) {
    override def path(p: OutputPath) = p match {
      case OutputPath.ServerSource => "app"
      case OutputPath.ServerTest => "test"
      case OutputPath.SharedSource => "shared/src/main/scala"
      case OutputPath.SharedTest => "shared/src/test/scala"
      case OutputPath.WikiMarkdown => "doc/src/main/paradox"
      case _ => super.path(p)
    }
  }

  case object Custom extends ProjectTemplate(
    value = "custom",
    title = "Custom",
    description = "A custom template allows you to specify default options manually",
    repo = "",
    icon = Icons.project,
    features = Feature.values.toSet
  )

  override val values = findValues
}
