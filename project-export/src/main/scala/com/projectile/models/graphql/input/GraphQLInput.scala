package com.projectile.models.graphql.input

import com.projectile.models.export.{ExportEnum, ExportModel, ExportService}
import com.projectile.models.graphql.input.GraphQLOptions.SchemaQueries
import com.projectile.models.input.{Input, InputSummary, InputTemplate}

object GraphQLInput {
  def fromSummary(is: InputSummary, schema: Seq[GraphQLOptions.SchemaQueries]) = {
    GraphQLInput(key = is.key, title = is.title, description = is.description, schema = schema)
  }
}

case class GraphQLInput(
    override val key: String = "new",
    override val title: String = "New GraphQL Input",
    override val description: String = "...",
    schema: Seq[SchemaQueries] = Nil,
) extends Input {
  override def template = InputTemplate.GraphQL

  override def exportEnum(key: String): ExportEnum = throw new IllegalStateException("TODO")

  override lazy val exportEnums: Seq[ExportEnum] = Nil

  override def exportModel(k: String): ExportModel = throw new IllegalStateException("TODO")

  override lazy val exportModels: Seq[ExportModel] = Nil

  override def exportService(k: String): ExportService = throw new IllegalStateException("TODO")

  override def exportServices: Seq[ExportService] = Nil
}
