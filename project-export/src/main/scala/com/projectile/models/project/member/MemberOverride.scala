package com.projectile.models.project.member

import com.projectile.util.JsonSerializers._

object MemberOverride {
  implicit val jsonEncoder: Encoder[MemberOverride] = deriveEncoder
  implicit val jsonDecoder: Decoder[MemberOverride] = deriveDecoder
}

case class MemberOverride(
    k: String,
    v: String
)
