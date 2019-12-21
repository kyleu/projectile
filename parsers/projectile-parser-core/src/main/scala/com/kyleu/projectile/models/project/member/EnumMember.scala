package com.kyleu.projectile.models.project.member

import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.util.JsonSerializers._

object EnumMember {
  implicit val jsonEncoder: Encoder[EnumMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[EnumMember] = deriveDecoder
}

final case class EnumMember(
    key: String,
    pkg: Seq[String] = Nil,
    features: Set[EnumFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
