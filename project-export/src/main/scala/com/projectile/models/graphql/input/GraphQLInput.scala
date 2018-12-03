package com.projectile.models.graphql.input

import com.projectile.models.export.{ExportEnum, ExportModel, ExportService}
import com.projectile.models.input.{Input, InputSummary, InputTemplate}
import sangria.ast.Document

object GraphQLInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = GraphQLInput(key = is.key, title = is.title, description = is.description, files = files)
}

case class GraphQLInput(
    override val key: String = "new",
    override val title: String = "New GraphQL Input",
    override val description: String = "...",
    files: Seq[String] = Nil,
    document: Document = Document.emptyStub
) extends Input {
  override def template = InputTemplate.GraphQL

  override def exportEnum(key: String): ExportEnum = throw new IllegalStateException("TODO")

  override lazy val exportEnums: Seq[ExportEnum] = Nil

  override def exportModel(k: String): ExportModel = throw new IllegalStateException("TODO")

  override lazy val exportModels: Seq[ExportModel] = Nil

  override def exportService(k: String): ExportService = throw new IllegalStateException("TODO")

  override def exportServices: Seq[ExportService] = Nil
}
