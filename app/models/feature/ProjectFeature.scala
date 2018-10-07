package models.feature

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class ProjectFeature(val title: String, val tech: String, val description: String) extends EnumEntry

object ProjectFeature extends Enum[ProjectFeature] with CirceEnum[ProjectFeature] {
  case object Core extends ProjectFeature("Core", "Scala", "Generates Scala case classes")
  case object Wiki extends ProjectFeature("Wiki", "Markdown", "Generates markdown documentation")

  override val values = findValues
}
