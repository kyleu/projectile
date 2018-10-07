package models.command

import enumeratum.{Enum, EnumEntry}
import models.input.Input
import models.project.Project

sealed trait ProjectileResponse extends EnumEntry

object ProjectileResponse extends Enum[ProjectileResponse] {
  case object OK extends ProjectileResponse
  case class Error(msg: String) extends ProjectileResponse

  case class ProjectList(projects: Seq[Project]) extends ProjectileResponse
  case class ProjectDetail(project: Project) extends ProjectileResponse

  case class InputList(inputs: Seq[Input]) extends ProjectileResponse
  case class InputDetail(input: Input) extends ProjectileResponse

  override val values = findValues
}
