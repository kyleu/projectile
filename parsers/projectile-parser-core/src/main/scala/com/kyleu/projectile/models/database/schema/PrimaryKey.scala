package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.util.JsonSerializers._

object PrimaryKey {
  implicit val jsonEncoder: Encoder[PrimaryKey] = deriveEncoder
  implicit val jsonDecoder: Decoder[PrimaryKey] = deriveDecoder
}

final case class PrimaryKey(
    name: String,
    columns: List[String]
)
