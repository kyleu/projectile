package models.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPackage(override val value: String, val defaultVal: Seq[String]) extends StringEnumEntry

object OutputPackage extends StringEnum[OutputPackage] with StringCirceEnum[OutputPackage] {
  case object System extends OutputPackage("system", Nil)
  case object Application extends OutputPackage("application", Nil)

  case object ResultModel extends OutputPackage("result-model", Seq("models", "result"))

  override val values = findValues
}
