package models.project

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.project.feature.ProjectFeature
import models.template.Icons

sealed abstract class ProjectTemplate(
    override val value: String,
    val title: String,
    val description: String,
    val icon: String,
    val features: Set[ProjectFeature]
) extends StringEnumEntry

object ProjectTemplate extends StringEnum[ProjectTemplate] with StringCirceEnum[ProjectTemplate] {
  case object Simple extends ProjectTemplate(
    value = "simple-play-template",
    title = "Simple",
    description = "A simple Scala Play Framework application with some useful defaults and helper classes",
    icon = Icons.library,
    features = Set.empty[ProjectFeature]
  )

  case object Boilerplay extends ProjectTemplate(
    value = "boilerplay",
    title = "Boilerplay",
    description = "Constantly updated, Boilerplay is a starter web application with loads of features",
    icon = Icons.web,
    features = Set.empty[ProjectFeature]
  )

  case object Custom extends ProjectTemplate(
    value = "custom",
    title = "Custom",
    description = "A custom template allows you to specify default options manually",
    icon = Icons.project,
    features = Set.empty[ProjectFeature]
  )

  override val values = findValues
}
