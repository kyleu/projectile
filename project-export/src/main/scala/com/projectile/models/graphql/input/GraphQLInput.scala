package com.projectile.models.graphql.input

import com.projectile.models.export.ExportService
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

  lazy val parsedObjects = parsedDocuments.flatMap {
    case (k, doc) => GraphQLDocumentParser.parse(parsedSchema(k), doc)
  }.toSeq.distinct

  override lazy val exportEnums = parsedObjects.collect { case Left(x) => x }
  override def exportEnum(k: String) = exportEnums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"No enum defined with key [$k] among candidates [${exportEnums.map(_.key).mkString(", ")}]")
  }

  override lazy val exportModels = parsedObjects.collect { case Right(x) => x }
  override def exportModel(k: String) = exportModels.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"No model defined with key [$k] among candidates [${exportModels.map(_.key).mkString(", ")}]")
  }

  override def template = InputTemplate.GraphQL

  override def exportServices = Seq.empty[ExportService]
  override def exportService(k: String) = exportServices.find(_.key == k).getOrElse(throw new IllegalStateException(s"No service defined with key [$k]"))
}
