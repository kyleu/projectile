package com.projectile.models.project.member

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.output.feature.EnumFeature
import com.projectile.util.JsonSerializers._

object EnumMember {
  sealed abstract class InputType(override val value: String) extends StringEnumEntry

  object InputType extends StringEnum[InputType] with StringCirceEnum[InputType] {
    case object PostgresEnum extends InputType(value = "postgres-enum")
    case object ThriftIntEnum extends InputType(value = "thrift-int-enum")
    case object ThriftStringEnum extends InputType(value = "thrift-string-enum")

    override val values = findValues
  }

  implicit val jsonEncoder: Encoder[EnumMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[EnumMember] = deriveDecoder
}

case class EnumMember(
    input: String,
    inputType: EnumMember.InputType,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[EnumFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
