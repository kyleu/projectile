package models.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPath(override val value: String) extends StringEnumEntry

object OutputPath extends StringEnum[OutputPath] with StringCirceEnum[OutputPath] {
  case object Root extends OutputPath("root")
  case object ServerSource extends OutputPath("server")
  case object SharedSource extends OutputPath("shared")

  override val values = findValues
}
