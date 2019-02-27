package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.util.JsonSerializers._

object ForeignKey {
  implicit val jsonEncoder: Encoder[ForeignKey] = deriveEncoder
  implicit val jsonDecoder: Decoder[ForeignKey] = deriveDecoder
}

case class ForeignKey(
    name: String,
    propertyName: String,
    targetTable: String,
    references: List[Reference]
)
