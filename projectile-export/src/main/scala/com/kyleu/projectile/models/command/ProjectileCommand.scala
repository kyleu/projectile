package com.kyleu.projectile.models.command

import enumeratum.{Enum, EnumEntry}
import com.kyleu.projectile.models.database.input.PostgresConnection
import com.kyleu.projectile.models.input.InputSummary
import com.kyleu.projectile.models.project._
import com.kyleu.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}
import com.kyleu.projectile.models.thrift.input.ThriftOptions
import com.kyleu.projectile.util.Version

sealed trait ProjectileCommand extends EnumEntry

object ProjectileCommand extends Enum[ProjectileCommand] {
  case object Doctor extends ProjectileCommand
  case object Init extends ProjectileCommand
  case object Testbed extends ProjectileCommand

  // Server
  case class ServerStart(port: Int = Version.projectPort) extends ProjectileCommand
  case object ServerStop extends ProjectileCommand

  // Inputs
  case class Inputs(key: Option[String]) extends ProjectileCommand

  case class InputRefresh(key: Option[String]) extends ProjectileCommand

  case class InputAdd(input: InputSummary) extends ProjectileCommand
  case class InputRemove(key: String) extends ProjectileCommand

  case class InputPostgresOptions(key: String, conn: PostgresConnection) extends ProjectileCommand
  case class InputThriftOptions(key: String, opts: ThriftOptions) extends ProjectileCommand

  // Audit
  case object Audit extends ProjectileCommand

  // Projects
  case class Projects(key: Option[String]) extends ProjectileCommand

  case class ProjectUpdate(key: Option[String]) extends ProjectileCommand
  case class ProjectExport(key: Option[String]) extends ProjectileCommand

  case class ProjectCodegen(key: Option[String]) extends ProjectileCommand

  case class ProjectAdd(project: ProjectSummary) extends ProjectileCommand
  case class ProjectSave(project: Project) extends ProjectileCommand
  case class ProjectRemove(key: String) extends ProjectileCommand

  case class SaveEnumMembers(project: String, members: Seq[EnumMember]) extends ProjectileCommand
  case class RemoveEnumMember(key: String, member: String) extends ProjectileCommand

  case class SaveModelMembers(project: String, members: Seq[ModelMember]) extends ProjectileCommand
  case class RemoveModelMember(key: String, member: String) extends ProjectileCommand

  case class SaveServiceMembers(project: String, members: Seq[ServiceMember]) extends ProjectileCommand
  case class RemoveServiceMember(key: String, member: String) extends ProjectileCommand

  override val values = findValues
}
