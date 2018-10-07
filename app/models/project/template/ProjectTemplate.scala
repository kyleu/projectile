package models.project.template

import enumeratum.{CirceEnum, Enum, EnumEntry}
import models.project.feature.ProjectFeature

sealed abstract class ProjectTemplate(val key: String, val title: String, val description: String, val features: Set[ProjectFeature]) extends EnumEntry

object ProjectTemplate extends Enum[ProjectTemplate] with CirceEnum[ProjectTemplate] {

  case object Simple extends ProjectTemplate(
    key = "simple-play-template",
    title = "Simple",
    description = "A simple Scala Play Framework application with some useful defaults and helper classes",
    features = Set.empty[ProjectFeature]
  )

  case object Boilerplay extends ProjectTemplate(
    key = "boilerplay",
    title = "Boilerplay",
    description = "Constantly updated, Boilerplay is a starter web application with loads of features",
    features = Set.empty[ProjectFeature]
  )

  case object Custom extends ProjectTemplate(
    key = "custom",
    title = "Custom",
    description = "A custom template allows you to specify default options manually",
    features = Set.empty[ProjectFeature]
  )

  override val values = findValues
}
