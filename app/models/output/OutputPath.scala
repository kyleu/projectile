package models.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPath(override val value: String, val description: String) extends StringEnumEntry

object OutputPath extends StringEnum[OutputPath] with StringCirceEnum[OutputPath] {
  case object Root extends OutputPath("root", "The main directory where all files are generated")
  case object ServerSource extends OutputPath("server", "Scala source root for server-only classes")
  case object SharedSource extends OutputPath("shared", "Scala source root for shared classes")
  case object WikiMarkdown extends OutputPath("wiki", "Location to store generated wiki documentation")

  override val values = findValues
}
