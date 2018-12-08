package com.projectile.models.project.member

import com.projectile.models.feature.ServiceFeature
import com.projectile.util.JsonSerializers._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

object ServiceMember {
  sealed abstract class InputType(override val value: String) extends StringEnumEntry {
    override val toString = value
  }

  object InputType extends StringEnum[InputType] with StringCirceEnum[InputType] {
    case object ThriftService extends InputType(value = "thrift-service")

    override val values = findValues
  }

  implicit val jsonEncoder: Encoder[ServiceMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ServiceMember] = deriveDecoder
}

case class ServiceMember(
    input: String,
    inputType: ServiceMember.InputType,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[ServiceFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
