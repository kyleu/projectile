package com.kyleu.projectile.models.project.member

import com.kyleu.projectile.util.JsonSerializers._

object UnionMember {
  implicit val jsonEncoder: Encoder[UnionMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[UnionMember] = deriveDecoder
}

case class UnionMember(
    key: String,
    pkg: Seq[String] = Nil,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
