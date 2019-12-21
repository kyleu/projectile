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
  case object Init extends ProjectileCommand
  case object Testbed extends ProjectileCommand

  // Server
  final case class ServerStart(port: Int = Version.projectPort) extends ProjectileCommand
  case object ServerStop extends ProjectileCommand

  // Inputs
  final case class Inputs(key: Option[String]) extends ProjectileCommand

  final case class InputRefresh(key: Option[String]) extends ProjectileCommand

  final case class InputAdd(input: InputSummary) extends ProjectileCommand
  final case class InputRemove(key: String) extends ProjectileCommand

  final case class InputPostgresOptions(key: String, conn: PostgresConnection) extends ProjectileCommand
  final case class InputThriftOptions(key: String, opts: ThriftOptions) extends ProjectileCommand

  // Audit
  final case class Audit(fix: Boolean) extends ProjectileCommand

  // Examples
  final case class CreateExample(key: String, template: String, force: Boolean) extends ProjectileCommand

  // Projects
  final case class Projects(key: Option[String]) extends ProjectileCommand

  final case class ProjectUpdate(key: Option[String]) extends ProjectileCommand
  final case class ProjectExport(key: Option[String]) extends ProjectileCommand

  final case class Codegen(keys: Seq[String]) extends ProjectileCommand

  final case class ProjectAdd(project: ProjectSummary) extends ProjectileCommand
  final case class ProjectSave(project: Project) extends ProjectileCommand
  final case class ProjectRemove(key: String) extends ProjectileCommand

  final case class SetFeature(project: String, feature: String) extends ProjectileCommand
  final case class SetPackage(project: String, item: String, pkg: String) extends ProjectileCommand

  final case class SaveEnumMembers(project: String, members: Seq[EnumMember]) extends ProjectileCommand
  final case class RemoveEnumMember(key: String, member: String) extends ProjectileCommand

  final case class SaveModelMembers(project: String, members: Seq[ModelMember]) extends ProjectileCommand
  final case class RemoveModelMember(key: String, member: String) extends ProjectileCommand

  final case class SaveServiceMembers(project: String, members: Seq[ServiceMember]) extends ProjectileCommand
  final case class RemoveServiceMember(key: String, member: String) extends ProjectileCommand

  override val values = findValues
}
