package com.projectile.models.input

import com.projectile.util.JsonSerializers._

object InputSummary {
  implicit val jsonEncoder: Encoder[InputSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[InputSummary] = deriveDecoder
}

case class InputSummary(
    template: InputTemplate = InputTemplate.Postgres,
    key: String = "new",
    title: String = "New Input",
    description: String = "..."
) extends Ordered[InputSummary] {
  override def compare(that: InputSummary) = title.compare(that.title)
}
