package com.kyleu.projectile.models.graphql.input

import better.files.File
import com.kyleu.projectile.models.export.ExportService
import com.kyleu.projectile.models.graphql.input.GraphQLOptions.SchemaQueries
import com.kyleu.projectile.models.graphql.parse.GraphQLDocumentParser
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate, InputType}
import com.kyleu.projectile.services.graphql.GraphQLLoader

object GraphQLInput {
  def fromSummary(is: InputSummary, schema: Seq[GraphQLOptions.SchemaQueries], workingDir: File) = {
    GraphQLInput(key = is.key, description = is.description, schema = schema, workingDir = workingDir)
  }
}

case class GraphQLInput(
    override val key: String = "new",
    override val description: String = "...",
    schema: Seq[SchemaQueries] = Nil,
    workingDir: File
) extends Input {
  override def template = InputTemplate.GraphQL

  val parsedContents = schema.map(s => s.schema -> GraphQLLoader.load(workingDir, s)).toMap

  val parsedSchema = schema.map(s => s.schemaClass -> GraphQLLoader.parseSchema(parsedContents.apply(s.schema)._1)).toMap

  val parsedDocuments = schema.map { s =>
    val queryFiles = parsedContents.find(_._1 == s.schema).map(_._2._2.toMap).getOrElse {
      throw new IllegalStateException(s"Cannot load query document [$s]")
    }
    GraphQLLoader.parseQueryFiles(s, queryFiles)
  }.toMap

  val parsedObjects = parsedDocuments.flatMap {
    case (k, doc) => GraphQLDocumentParser.parse(Seq(k), parsedSchema(k), doc)
  }.toSeq.distinct

  override lazy val exportEnums = parsedObjects.collect { case Left(x) => x }
  override def exportEnum(k: String) = exportEnums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"No input enum defined with key [$k] among candidates [${exportEnums.map(_.key).sorted.mkString(", ")}]")
  }

  override lazy val exportModels = parsedObjects.collect { case Right(x) => x }
  override def exportModel(k: String) = exportModels.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"No input model defined with key [$k] among candidates [${exportModels.map(_.key).sorted.mkString(", ")}]")
  }

  override def exportServices = Seq.empty[ExportService]
  override def exportService(k: String) = exportServices.find(_.key == k).getOrElse(throw new IllegalStateException(s"No service defined with key [$k]"))
}
