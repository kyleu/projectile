package models.project.member

import util.JsonSerializers._

object MemberOverride {
  implicit val jsonEncoder: Encoder[MemberOverride] = deriveEncoder
  implicit val jsonDecoder: Decoder[MemberOverride] = deriveDecoder
}

case class MemberOverride(
    prop: String,
    v: String
)
