package com.kyleu.projectile.models.project

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ProjectFlag(
    override val value: String,
    val title: String,
    val desc: String
) extends StringEnumEntry

object ProjectFlag extends StringEnum[ProjectFlag] with StringCirceEnum[ProjectFlag] {
  case object Debug extends ProjectFlag(
    value = "debug",
    title = "Extra Debug Comments",
    desc = "Adds lots of extra comments to the generated code"
  )
  case object NoBulk extends ProjectFlag(
    value = "nobulk",
    title = "Don't Generate Bulk",
    desc = "Excludes bulk edit methods from the generated code"
  )

  override val values = findValues
  val set = values.toSet
}
