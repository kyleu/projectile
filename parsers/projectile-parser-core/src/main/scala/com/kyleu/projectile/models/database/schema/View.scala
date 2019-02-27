package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.util.JsonSerializers._

object View {
  implicit val jsonEncoder: Encoder[View] = deriveEncoder
  implicit val jsonDecoder: Decoder[View] = deriveDecoder
}

case class View(
    name: String,
    catalog: Option[String],
    schema: Option[String],
    description: Option[String],
    definition: Option[String],

    columns: Seq[Column] = Nil
)
