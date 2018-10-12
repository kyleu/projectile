package models.command

import enumeratum.{Enum, EnumEntry}
import models.input.InputSummary
import models.project._

sealed trait ProjectileCommand extends EnumEntry

object ProjectileCommand extends Enum[ProjectileCommand] {
  case object Doctor extends ProjectileCommand
  case object Init extends ProjectileCommand

  case class StartServer(port: Int = util.Version.projectPort) extends ProjectileCommand
  case object StopServer extends ProjectileCommand

  case object ListProjects extends ProjectileCommand
  case class GetProject(key: String) extends ProjectileCommand
  case class AddProject(project: ProjectSummary) extends ProjectileCommand

  case class SaveProject(project: Project) extends ProjectileCommand
  case class SaveProjectEnum(project: String, enum: ProjectEnum) extends ProjectileCommand
  case class SaveProjectModel(project: String, enum: ProjectModel) extends ProjectileCommand
  case class SaveProjectService(project: String, enum: ProjectSvc) extends ProjectileCommand

  case class RemoveProject(key: String) extends ProjectileCommand
  case class RemoveProjectEnum(key: String, enum: String) extends ProjectileCommand
  case class RemoveProjectModel(key: String, model: String) extends ProjectileCommand
  case class RemoveProjectService(key: String, service: String) extends ProjectileCommand

  case class ExportProject(key: String) extends ProjectileCommand
  case class AuditProject(key: String) extends ProjectileCommand

  case object ListInputs extends ProjectileCommand
  case class GetInput(key: String) extends ProjectileCommand
  case class AddInput(input: InputSummary) extends ProjectileCommand
  case class RemoveInput(key: String) extends ProjectileCommand
  case class RefreshInput(key: String) extends ProjectileCommand

  case object Testbed extends ProjectileCommand

  override val values = findValues
}
