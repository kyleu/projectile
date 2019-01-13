package com.kyleu.projectile.models.project.codegen

import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.models.project.audit.AuditResult
import com.kyleu.projectile.services.output.OutputService
import com.kyleu.projectile.util.JsonSerializers._

object CodegenResult {
  implicit val jsonEncoder: Encoder[CodegenResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[CodegenResult] = deriveDecoder
}

case class CodegenResult(
    updates: Seq[String],
    exportResults: Seq[(ProjectOutput, Seq[OutputService.WriteResult])],
    auditResults: Option[AuditResult],
    durationMs: Int
)
