package com.projectile.models.graphql.input

import com.projectile.models.export.{ExportEnum, ExportModel, ExportService}
import com.projectile.models.graphql.input.GraphQLOptions.SchemaQueries
import com.projectile.models.graphql.parse.GraphQLDocumentParser
import com.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.projectile.services.graphql.GraphQLLoader

object GraphQLInput {
  def fromSummary(is: InputSummary, schema: Seq[GraphQLOptions.SchemaQueries]) = {
    GraphQLInput(key = is.key, title = is.title, description = is.description, schema = schema)
  }
}

case class GraphQLInput(
    override val key: String = "new",
    override val title: String = "New GraphQL Input",
    override val description: String = "...",
    schema: Seq[SchemaQueries] = Nil
) extends Input {
  lazy val parsedContents = schema.map(s => s.schema -> GraphQLLoader.load(s)).toMap

  lazy val parsedSchema = schema.map(s => s.schemaClass -> GraphQLLoader.parseSchema(parsedContents.apply(s.schema)._1)).toMap

  lazy val parsedDocuments = schema.map(s => GraphQLLoader.parseQueryFiles(s, parsedContents.find(_._1 == s.schema).map(_._2._2.toMap).getOrElse {
    throw new IllegalStateException("Cannot load query document []")
  })).toMap

  lazy val parsedModels = parsedDocuments.map {
    case (k, doc) => k -> GraphQLDocumentParser.parse(parsedSchema(k), doc)
  }

  override def template = InputTemplate.GraphQL

  override lazy val exportEnums = Seq.empty[ExportEnum]
  override def exportEnum(k: String) = exportEnums.find(_.key == k).getOrElse(throw new IllegalStateException(s"No enum defined with key [$k]"))

  override lazy val exportModels = parsedModels.flatMap(_._2).toSeq
  override def exportModel(k: String) = exportModels.find(_.key == k).getOrElse(throw new IllegalStateException(s"No model defined with key [$k]"))

  override def exportServices = Seq.empty[ExportService]
  override def exportService(k: String) = exportServices.find(_.key == k).getOrElse(throw new IllegalStateException(s"No service defined with key [$k]"))
}
