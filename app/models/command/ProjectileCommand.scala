package models.command

import enumeratum.{Enum, EnumEntry}
import models.database.input.PostgresConnection
import models.input.InputSummary
import models.project._
import models.project.member.{EnumMember, ModelMember}

sealed trait ProjectileCommand extends EnumEntry

object ProjectileCommand extends Enum[ProjectileCommand] {
  case object Doctor extends ProjectileCommand
  case object Init extends ProjectileCommand
  case object Testbed extends ProjectileCommand

  case class StartServer(port: Int = util.Version.projectPort) extends ProjectileCommand
  case object StopServer extends ProjectileCommand

  case object ListProjects extends ProjectileCommand
  case class GetProject(key: String) extends ProjectileCommand
  case class AddProject(project: ProjectSummary) extends ProjectileCommand
  case class SaveProject(project: Project) extends ProjectileCommand
  case class RemoveProject(key: String) extends ProjectileCommand

  case class SaveModelMembers(project: String, members: Seq[ModelMember]) extends ProjectileCommand
  case class RemoveModelMember(key: String, member: String) extends ProjectileCommand

  case class SaveEnumMembers(project: String, members: Seq[EnumMember]) extends ProjectileCommand
  case class RemoveEnumMember(key: String, member: String) extends ProjectileCommand

  case class ExportProject(key: String) extends ProjectileCommand
  case class AuditProject(key: String) extends ProjectileCommand

  case object ListInputs extends ProjectileCommand
  case class GetInput(key: String) extends ProjectileCommand
  case class AddInput(input: InputSummary) extends ProjectileCommand
  case class SetPostgresOptions(key: String, conn: PostgresConnection) extends ProjectileCommand
  case class RemoveInput(key: String) extends ProjectileCommand
  case class RefreshInput(key: String) extends ProjectileCommand

  override val values = findValues
}
