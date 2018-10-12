package models.project

import util.JsonSerializers._

object ProjectEnum {
  implicit val jsonEncoder: Encoder[ProjectEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectEnum] = deriveDecoder
}

case class ProjectEnum(source: String)
