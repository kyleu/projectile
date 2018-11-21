package com.projectile.models.project.audit

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.project.ProjectOutput
import com.projectile.util.JsonSerializers._

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
