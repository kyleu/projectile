package com.kyleu.projectile.models.project.audit

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.util.JsonSerializers._

object AuditResult {
  implicit val jsonEncoder: Encoder[AuditResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[AuditResult] = deriveDecoder
}

case class AuditResult(
    config: ExportConfiguration,
    configMessages: Seq[AuditMessage],
    output: ProjectOutput,
    outputMessages: Seq[AuditMessage]
)
