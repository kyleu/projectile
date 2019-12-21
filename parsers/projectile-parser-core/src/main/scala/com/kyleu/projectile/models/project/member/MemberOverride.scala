package com.kyleu.projectile.models.project.member

import com.kyleu.projectile.util.JsonSerializers._

object MemberOverride {
  implicit val jsonEncoder: Encoder[MemberOverride] = deriveEncoder
  implicit val jsonDecoder: Decoder[MemberOverride] = deriveDecoder
}

final case class MemberOverride(
    k: String,
    v: String
)
