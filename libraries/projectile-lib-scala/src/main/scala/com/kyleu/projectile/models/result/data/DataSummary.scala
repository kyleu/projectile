package com.kyleu.projectile.models.result.data

import com.kyleu.projectile.util.JsonSerializers._

object DataSummary {
  implicit val jsonEncoder: Encoder[DataSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[DataSummary] = deriveDecoder
}

case class DataSummary(
    model: String,
    pk: Seq[String],
    title: String
)
