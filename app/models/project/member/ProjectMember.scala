package models.project.member

import util.JsonSerializers._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import models.output.feature.Feature

object ProjectMember {
  sealed abstract class OutputType(override val value: String) extends StringEnumEntry

  object OutputType extends StringEnum[OutputType] with StringCirceEnum[OutputType] {
    case object Enum extends OutputType(value = "enum")
    case object Model extends OutputType(value = "model")

    override val values = findValues
  }

  sealed abstract class InputType(override val value: String, val out: OutputType) extends StringEnumEntry

  object InputType extends StringEnum[InputType] with StringCirceEnum[InputType] {
    case object PostgresEnum extends InputType(value = "postgres-enum", OutputType.Enum)
    case object PostgresTable extends InputType(value = "postgres-table", OutputType.Model)
    case object PostgresView extends InputType(value = "postgres-view", OutputType.Model)

    override val values = findValues
  }

  implicit val jsonEncoder: Encoder[ProjectMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectMember] = deriveDecoder
}

case class ProjectMember(
    input: String,
    inputType: ProjectMember.InputType,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[Feature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)

  lazy val outputType = inputType.out
}
