package services.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPath(override val value: String, defaultVal: String) extends StringEnumEntry

object OutputPath extends StringEnum[OutputPath] with StringCirceEnum[OutputPath] {
  case object Root extends OutputPath("root", ".")
  case object ServerSource extends OutputPath("server", "app")
  case object SharedSource extends OutputPath("shared", "shared/src/main/scala")

  override val values = findValues
}
