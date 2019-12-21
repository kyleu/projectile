package com.kyleu.projectile.models.project.audit

import com.kyleu.projectile.util.JsonSerializers._

object AuditResult {
  implicit val jsonEncoder: Encoder[AuditResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[AuditResult] = deriveDecoder
}

final case class AuditResult(
    configMessages: Seq[AuditMessage],
    outputMessages: Seq[AuditMessage]
)
