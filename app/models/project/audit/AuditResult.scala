package models.project.audit

import models.export.config.ExportConfiguration
import models.project.ProjectOutput
import util.JsonSerializers._

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
