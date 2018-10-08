package models.database.input

import util.JsonSerializers._

object PostgresConnection {
  val key = "postgres"

  implicit val jsonEncoder: Encoder[PostgresConnection] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresConnection] = deriveDecoder
}

case class PostgresConnection(
    url: String,
    username: String,
    password: String,
    db: String,
    catalog: Option[String]
)
