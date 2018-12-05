package com.projectile.models.graphql.input

import com.projectile.util.JsonSerializers._

object GraphQLOptions {
  object SchemaQueries {
    implicit val jsonEncoder: Encoder[SchemaQueries] = deriveEncoder
    implicit val jsonDecoder: Decoder[SchemaQueries] = deriveDecoder
  }

  case class SchemaQueries(schema: String, queryFiles: Seq[String] = Nil) {
    val schemaClass = schema.lastIndexOf('/') match {
      case -1 => schema
      case x => schema.substring(x + 1)
    }
    val fileClasses = queryFiles.map { f =>
      val c = f.lastIndexOf('/') match {
        case -1 => f
        case x => f.substring(x + 1)
      }
      c -> f
    }
  }

  implicit val jsonEncoder: Encoder[GraphQLOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[GraphQLOptions] = deriveDecoder
}

case class GraphQLOptions(schema: Seq[GraphQLOptions.SchemaQueries] = Nil)
