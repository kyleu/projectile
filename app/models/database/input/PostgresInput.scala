package models.input

import models.database.schema.{EnumType, Procedure, Table, View}
import util.JsonSerializers._

object PostgresInput {
  val key = "postgres"

  implicit val jsonEncoder: Encoder[PostgresInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInput] = deriveDecoder
}

case class PostgresInput(
    key: String,
    title: String,
    description: String,
    url: String,
    username: String,
    password: String,
    db: String,
    catalog: Option[String],
    enums: Seq[EnumType],
    tables: Seq[Table],
    views: Seq[View],
    procedures: Seq[Procedure]
)
