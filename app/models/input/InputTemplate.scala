package models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.template.Icons

sealed abstract class InputTemplate(
    override val value: String,
    val title: String,
    val description: String,
    val icon: String
) extends StringEnumEntry

object InputTemplate extends StringEnum[InputTemplate] with StringCirceEnum[InputTemplate] {

  case object Postgres extends InputTemplate(
    value = "postgres",
    title = "PostgreSQL Database",
    description = "Tables, views, and enums from a single Postgres database",
    icon = Icons.database
  )

  case object Filesystem extends InputTemplate(
    value = "filesystem",
    title = "Filesystem",
    description = "Loads csv and json files from the filesystem",
    icon = Icons.database
  )

  override val values = findValues
}
