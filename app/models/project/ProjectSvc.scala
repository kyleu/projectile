package models.project

import util.JsonSerializers._

object ProjectSvc {
  implicit val jsonEncoder: Encoder[ProjectSvc] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectSvc] = deriveDecoder
}

case class ProjectSvc(source: String)
