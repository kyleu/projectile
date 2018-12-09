package com.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.template.Icons

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

  case object Thrift extends InputTemplate(
    value = "thrift",
    title = "Thrift Definitions",
    description = "Enums, structs, and services from a collection of Thrift IDL files",
    icon = Icons.thrift
  )

  case object GraphQL extends InputTemplate(
    value = "graphql",
    title = "GraphQL Queries",
    description = "GraphQL query models from a file containing GraphQL queries",
    icon = Icons.graphql
  )

  case object Filesystem extends InputTemplate(
    value = "filesystem",
    title = "Filesystem",
    description = "Loads csv and json files from the filesystem",
    icon = Icons.file
  )

  override val values = findValues
}