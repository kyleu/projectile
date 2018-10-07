package models.project

import util.JsonSerializers._

object Project {
  implicit val jsonEncoder: Encoder[Project] = deriveEncoder
  implicit val jsonDecoder: Decoder[Project] = deriveDecoder
}

case class Project(
    key: String,
    title: String,
    description: String
)
