package com.projectile.models.graphql.input

import com.projectile.util.JsonSerializers._

object GraphQLOptions {
  object SchemaQueries {
    implicit val jsonEncoder: Encoder[SchemaQueries] = deriveEncoder
    implicit val jsonDecoder: Decoder[SchemaQueries] = deriveDecoder
  }

  case class SchemaQueries(schema: String, queryFiles: Seq[String] = Nil)

  implicit val jsonEncoder: Encoder[GraphQLOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[GraphQLOptions] = deriveDecoder
}

case class GraphQLOptions(schema: Seq[GraphQLOptions.SchemaQueries] = Nil)
