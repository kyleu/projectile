package com.kyleu.projectile.models.graphql.input

import com.kyleu.projectile.util.JsonSerializers._

object GraphQLOptions {
  object SchemaQueries {
    implicit val jsonEncoder: Encoder[SchemaQueries] = deriveEncoder
    implicit val jsonDecoder: Decoder[SchemaQueries] = deriveDecoder
  }

  case class SchemaQueries(schema: String, queryFiles: Seq[String] = Nil) {
    private[this] def fix(s: String) = s.lastIndexOf('/') match {
      case -1 => s.stripSuffix(".graphql")
      case x => s.substring(x + 1).stripSuffix(".graphql")
    }
    val schemaClass = fix(schema)
    val fileClasses = queryFiles.map(f => fix(f) -> f)
  }

  implicit val jsonEncoder: Encoder[GraphQLOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[GraphQLOptions] = deriveDecoder

  val empty = GraphQLOptions(schema = Seq(SchemaQueries("schema.graphql", Seq("queries.graphql"))))
}

case class GraphQLOptions(schema: Seq[GraphQLOptions.SchemaQueries] = Nil)
