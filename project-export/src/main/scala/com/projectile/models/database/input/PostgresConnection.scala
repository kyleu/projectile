package com.projectile.models.database.input

import com.projectile.util.JsonSerializers._

object PostgresConnection {
  val key = "postgres"

  implicit val jsonEncoder: Encoder[PostgresConnection] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresConnection] = deriveDecoder
}

case class PostgresConnection(
    url: String = "jdbc:postgresql://localhost/db",
    username: String = "",
    password: String = "",
    ssl: Boolean = false,
    db: String = "db",
    catalog: Option[String] = None
)