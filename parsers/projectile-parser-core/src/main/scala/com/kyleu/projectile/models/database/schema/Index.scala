package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.util.JsonSerializers._

object Index {
  implicit val jsonEncoder: Encoder[Index] = deriveEncoder
  implicit val jsonDecoder: Decoder[Index] = deriveDecoder
}

final case class Index(
    name: String,
    unique: Boolean,
    indexType: String,
    columns: Seq[IndexColumn]
)
