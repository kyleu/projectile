package models.command

import enumeratum.{Enum, EnumEntry}

sealed trait ProjectileCommand extends EnumEntry

object ProjectileCommand extends Enum[ProjectileCommand] {
  case object Doctor extends ProjectileCommand
  case object Init extends ProjectileCommand

  case class StartServer(port: Int = util.Version.projectPort) extends ProjectileCommand
  case object StopServer extends ProjectileCommand

  case class AddProject(key: String, todo: String) extends ProjectileCommand
  case object ListProjects extends ProjectileCommand
  case class GetProject(key: String) extends ProjectileCommand
  case class RefreshProject(key: String) extends ProjectileCommand
  case class ExportProject(key: String) extends ProjectileCommand

  case class AddInput(key: String, todo: String) extends ProjectileCommand
  case object ListInputs extends ProjectileCommand
  case class GetInput(key: String) extends ProjectileCommand
  case class RefreshInput(key: String) extends ProjectileCommand

  override val values = findValues
}
