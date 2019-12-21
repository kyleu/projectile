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
  final case class OK(msg: String) extends ProjectileResponse
  final case class Error(msg: String) extends ProjectileResponse
  final case class JsonResponse(json: Json) extends ProjectileResponse

  final case class InputList(inputs: Seq[InputSummary]) extends ProjectileResponse
  final case class InputDetail(input: Input) extends ProjectileResponse
  final case class InputResults(results: Seq[InputDetail]) extends ProjectileResponse

  final case class ProjectList(projects: Seq[ProjectSummary]) extends ProjectileResponse
  final case class ProjectDetail(project: Project) extends ProjectileResponse

  final case class ProjectUpdateResult(key: String, log: Seq[String]) extends ProjectileResponse
  final case class ProjectExportResult(output: ProjectOutput, files: Seq[OutputWriteResult]) extends ProjectileResponse
  final case class ProjectAuditResult(result: AuditResult, fixed: Seq[String]) extends ProjectileResponse
  final case class ProjectCodegenResult(result: CodegenResult) extends ProjectileResponse

  final case class CompositeResult(results: Seq[ProjectileResponse]) extends ProjectileResponse

  override val values = findValues
}
