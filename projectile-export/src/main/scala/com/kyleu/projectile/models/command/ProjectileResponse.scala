package com.kyleu.projectile.models.command

import enumeratum.{Enum, EnumEntry}
import io.circe.Json
import com.kyleu.projectile.models.input.{Input, InputSummary}
import com.kyleu.projectile.models.output.OutputWriteResult
import com.kyleu.projectile.models.project.audit.AuditResult
import com.kyleu.projectile.models.project.codegen.CodegenResult
import com.kyleu.projectile.models.project.{Project, ProjectOutput, ProjectSummary}

sealed trait ProjectileResponse extends EnumEntry

object ProjectileResponse extends Enum[ProjectileResponse] {
  case class OK(msg: String) extends ProjectileResponse
  case class Error(msg: String) extends ProjectileResponse
  case class JsonResponse(json: Json) extends ProjectileResponse

  case class InputList(inputs: Seq[InputSummary]) extends ProjectileResponse
  case class InputDetail(input: Input) extends ProjectileResponse
  case class InputResults(results: Seq[InputDetail]) extends ProjectileResponse

  case class ProjectList(projects: Seq[ProjectSummary]) extends ProjectileResponse
  case class ProjectDetail(project: Project) extends ProjectileResponse

  case class ProjectUpdateResult(key: String, log: Seq[String]) extends ProjectileResponse
  case class ProjectExportResult(output: ProjectOutput, files: Seq[OutputWriteResult]) extends ProjectileResponse
  case class ProjectAuditResult(result: AuditResult, fixed: Seq[String]) extends ProjectileResponse
  case class ProjectCodegenResult(result: CodegenResult) extends ProjectileResponse

  case class CompositeResult(results: Seq[ProjectileResponse]) extends ProjectileResponse

  override val values = findValues
}
