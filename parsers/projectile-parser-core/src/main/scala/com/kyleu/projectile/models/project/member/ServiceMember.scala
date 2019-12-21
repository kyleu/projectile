package com.kyleu.projectile.models.project.member

import com.kyleu.projectile.models.feature.ServiceFeature
import com.kyleu.projectile.util.JsonSerializers._

object ServiceMember {
  implicit val jsonEncoder: Encoder[ServiceMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ServiceMember] = deriveDecoder
}

final case class ServiceMember(
    key: String,
    pkg: Seq[String] = Nil,

    features: Set[ServiceFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
