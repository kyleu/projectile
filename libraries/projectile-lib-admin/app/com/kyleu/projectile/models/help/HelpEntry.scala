package com.kyleu.projectile.models.help

import com.kyleu.projectile.util.JsonSerializers._

object HelpEntry {
  implicit val jsonEncoder: Encoder[HelpEntry] = deriveEncoder
  implicit val jsonDecoder: Decoder[HelpEntry] = deriveDecoder
}

case class HelpEntry(
    path: List[String],
    title: String,
    description: Option[String],
    icon: Option[String],
    url: Option[String],
    content: Option[String],
    children: Seq[HelpEntry]
)
