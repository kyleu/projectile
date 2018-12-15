package com.kyleu.projectile.services.graphql

import better.files.File
import com.kyleu.projectile.models.graphql.input.GraphQLOptions
import sangria.ast.Document
import sangria.parser.QueryParser

object GraphQLLoader {
  private[this] def check(t: String, f: File) = if (!(f.exists && f.isRegularFile && f.isReadable)) {
    throw new IllegalStateException(s"Cannot read [$t] from [${f.pathAsString}]")
  }

  def load(workingDir: File, options: GraphQLOptions.SchemaQueries) = {
    val schemaFile = workingDir / options.schema
    check("schema", schemaFile)
    schemaFile.contentAsString -> options.fileClasses.map { q =>
      val qf = workingDir / q._2
      check("queries", qf)
      q._1 -> qf.contentAsString
    }
  }

  def parseSchema(schemaContent: String) = {
    import sangria.parser.QueryParser
    import sangria.schema.Schema

    import scala.util.{Failure, Success}

    Schema.buildFromAst(QueryParser.parse(schemaContent) match {
      case Success(s) => s
      case Failure(x) => throw new IllegalStateException(s"Error loading schema", x)
    })
  }

  def parseQueryFiles(s: GraphQLOptions.SchemaQueries, parsedContents: Map[String, String]) = {
    val docs = s.fileClasses.map(f => QueryParser.parse(parsedContents(f._1)).get)
    s.schemaClass -> docs.foldLeft(Document.emptyStub)((d, f) => d.merge(f))
  }
}
