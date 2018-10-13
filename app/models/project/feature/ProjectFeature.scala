package models.project.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ProjectFeature(override val value: String, val title: String, val tech: String, val description: String) extends StringEnumEntry

object ProjectFeature extends StringEnum[ProjectFeature] with StringCirceEnum[ProjectFeature] {
  case object Core extends ProjectFeature("core", "Core", "Scala", "Scala case classes and Circe Json serializers")
  case object Wiki extends ProjectFeature("wiki", "Wiki", "Markdown", "Markdown documentation in Github wiki format")

  override val values = findValues
}
