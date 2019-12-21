package com.kyleu.projectile.models.project.codegen

import com.kyleu.projectile.models.output.OutputWriteResult
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.models.project.audit.AuditResult
import com.kyleu.projectile.util.JsonSerializers._

object CodegenResult {
  implicit val jsonEncoder: Encoder[CodegenResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[CodegenResult] = deriveDecoder
}

final case class CodegenResult(
    updates: Seq[String],
    exportResults: Seq[(ProjectOutput, Seq[OutputWriteResult])],
    auditResults: Option[AuditResult],
    durationMs: Int
)
