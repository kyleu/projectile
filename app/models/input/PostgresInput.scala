package models.input

import models.database.schema.Schema
import util.JsonSerializers._

object PostgresInput {
  implicit val jsonEncoder: Encoder[PostgresInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInput] = deriveDecoder
}

case class PostgresInput(
    key: String,
    title: String,
    description: String,
    schema: Schema
)
