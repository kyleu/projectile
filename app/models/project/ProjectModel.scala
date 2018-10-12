package models.project

import util.JsonSerializers._

object ProjectModel {
  implicit val jsonEncoder: Encoder[ProjectModel] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectModel] = deriveDecoder
}

case class ProjectModel(source: String)
