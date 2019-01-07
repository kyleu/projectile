package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.util.JsonSerializers._

object EnumType {
  implicit val jsonEncoder: Encoder[EnumType] = deriveEncoder
  implicit val jsonDecoder: Decoder[EnumType] = deriveDecoder
}

case class EnumType(
    key: String,
    values: Seq[String]
)
