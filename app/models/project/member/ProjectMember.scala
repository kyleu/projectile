package models.project.member

import util.JsonSerializers._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

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
    inputKey: String,

    outputKey: String,
    outputPackage: Seq[String] = Nil,

    ignored: Seq[String] = Nil,
    overrides: Seq[MemberOverride] = Nil
) {
  lazy val outputType = inputType.out
}
