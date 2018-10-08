package models.input

import util.JsonSerializers._

object InputSummary {
  implicit val jsonEncoder: Encoder[InputSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[InputSummary] = deriveDecoder
}

case class InputSummary(
    t: InputTemplate,
    key: String,
    title: String,
    description: String
) extends Ordered[InputSummary] {
  override def compare(that: InputSummary) = title.compare(that.title)
}
