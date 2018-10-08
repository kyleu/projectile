package models.input

import util.JsonSerializers._

object PostgresInputSummary {
  val key = "postgres"

  implicit val jsonEncoder: Encoder[PostgresInputSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInputSummary] = deriveDecoder
}

case class PostgresInputSummary(
    url: String,
    username: String,
    password: String,
    db: String,
    catalog: Option[String]
)
