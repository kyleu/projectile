package com.projectile.services.graphql

import better.files._
import com.projectile.models.graphql.input.GraphQLInput
import sangria.ast.Document
import sangria.parser.QueryParser

object GraphQLParseService {
  def loadGraphQLInput(files: Seq[File], t: GraphQLInput) = {
    val doc = files.foldLeft(Document.emptyStub)((d, f) => d.merge(QueryParser.parse(f.contentAsString).get))
    t.copy(document = doc)
  }
}
