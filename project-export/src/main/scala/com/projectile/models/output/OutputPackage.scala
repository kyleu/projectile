package com.projectile.models.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPackage(override val value: String, val defaultVal: Seq[String]) extends StringEnumEntry

object OutputPackage extends StringEnum[OutputPackage] with StringCirceEnum[OutputPackage] {
  case object System extends OutputPackage("system", Nil)
  case object Application extends OutputPackage("application", Nil)

  case object Utils extends OutputPackage("utilities", Seq("util"))
  case object Results extends OutputPackage("results", Seq("models", "result"))
  case object Tags extends OutputPackage("tags", Seq("models", "tag"))

  override val values = findValues
}
