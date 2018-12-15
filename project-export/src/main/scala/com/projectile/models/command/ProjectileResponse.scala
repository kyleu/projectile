package com.projectile.models.command

import enumeratum.{Enum, EnumEntry}
import io.circe.Json
import com.projectile.models.input.{Input, InputSummary}
import com.projectile.models.project.audit.AuditResult
import com.projectile.models.project.{Project, ProjectOutput, ProjectSummary}
import com.projectile.services.output.OutputService

sealed trait ProjectileResponse extends EnumEntry

object ProjectileResponse extends Enum[ProjectileResponse] {
  case object OK extends ProjectileResponse
  case class Error(msg: String) extends ProjectileResponse
  case class JsonResponse(json: Json) extends ProjectileResponse

  case class InputList(inputs: Seq[InputSummary]) extends ProjectileResponse
  case class InputDetail(input: Input) extends ProjectileResponse
  case class InputResults(results: Seq[InputDetail]) extends ProjectileResponse

  case class ProjectList(projects: Seq[ProjectSummary]) extends ProjectileResponse
  case class ProjectDetail(project: Project) extends ProjectileResponse

  case class ProjectUpdateResult(key: String, log: Seq[String]) extends ProjectileResponse
  case class ProjectExportResult(output: ProjectOutput, files: Seq[OutputService.WriteResult]) extends ProjectileResponse
  case class ProjectAuditResult(result: AuditResult) extends ProjectileResponse

  case class CompositeResult(results: Seq[ProjectileResponse]) extends ProjectileResponse

  override val values = findValues
}
