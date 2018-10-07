package models.input

import util.JsonSerializers._

object Input {
  implicit val jsonEncoder: Encoder[Input] = deriveEncoder
  implicit val jsonDecoder: Decoder[Input] = deriveDecoder
}

case class Input(
    key: String,
    title: String,
    description: String
)
