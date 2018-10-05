package models.command

import enumeratum.{Enum, EnumEntry}

sealed trait ProjectileCommand extends EnumEntry

object ProjectileCommand extends Enum[ProjectileCommand] {
  case object Doctor extends ProjectileCommand
  case object Init extends ProjectileCommand

  case class StartServer(port: Int = util.Version.projectPort) extends ProjectileCommand

  case object ListProjects extends ProjectileCommand

  override val values = findValues
}
