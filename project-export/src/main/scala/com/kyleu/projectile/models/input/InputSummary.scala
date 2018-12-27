package com.kyleu.projectile.models.input

import com.kyleu.projectile.util.JsonSerializers._

object InputSummary {
  implicit val jsonEncoder: Encoder[InputSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[InputSummary] = deriveDecoder
}

case class InputSummary(
    template: InputTemplate = InputTemplate.Postgres,
    key: String = "new",
    description: String = "..."
) extends Ordered[InputSummary] {
  override def compare(that: InputSummary) = key.compare(that.key)
}
