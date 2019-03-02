package com.kyleu.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.kyleu.projectile.models.template.Icons

sealed abstract class InputTemplate(
    override val value: String,
    val title: String,
    val description: String,
    val icon: String
) extends StringEnumEntry {
  def supportsUnion = false
  def supportsService = false
}

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
  ) {
    override val supportsUnion = true
    override val supportsService = true
  }

  case object GraphQL extends InputTemplate(
    value = "graphql",
    title = "GraphQL Queries",
    description = "GraphQL query models from a file containing GraphQL queries",
    icon = Icons.graphql
  ) {
    override val supportsUnion = true
  }

  override val values = findValues
  val all = values.toSet
}
