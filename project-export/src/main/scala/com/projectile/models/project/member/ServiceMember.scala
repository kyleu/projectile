package com.projectile.models.project.member

import com.projectile.models.feature.ServiceFeature
import com.projectile.util.JsonSerializers._

object ServiceMember {
  implicit val jsonEncoder: Encoder[ServiceMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ServiceMember] = deriveDecoder
}

case class ServiceMember(
    input: String,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[ServiceFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
