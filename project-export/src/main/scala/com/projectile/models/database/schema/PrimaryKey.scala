package com.projectile.models.database.schema

import com.projectile.util.JsonSerializers._

object PrimaryKey {
  implicit val jsonEncoder: Encoder[PrimaryKey] = deriveEncoder
  implicit val jsonDecoder: Decoder[PrimaryKey] = deriveDecoder
}

case class PrimaryKey(
    name: String,
    columns: List[String]
)
