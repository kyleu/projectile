package com.projectile.models.project.member

import com.projectile.models.feature.ModelFeature
import com.projectile.util.JsonSerializers._

object ModelMember {
  implicit val jsonEncoder: Encoder[ModelMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ModelMember] = deriveDecoder
}

case class ModelMember(
    input: String,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[ModelFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
