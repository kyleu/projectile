package com.kyleu.projectile.models.result.data

import com.kyleu.projectile.util.JsonSerializers._

object DataSummary {
  implicit val jsonEncoder: Encoder[DataSummary] = (r: DataSummary) => io.circe.Json.obj(
    ("model", r.model.asJson),
    ("pk", r.pk.asJson),
    ("entries", r.entries.asJson),
    ("title", r.title.asJson)
  )
  implicit val jsonDecoder: Decoder[DataSummary] = deriveDecoder
}

final case class DataSummary(model: String, pk: String, entries: Map[String, Option[String]]) {
  lazy val title = entries.map(e => s"${e._1}: ${e._2.getOrElse("∅")}").mkString(", ")
}
