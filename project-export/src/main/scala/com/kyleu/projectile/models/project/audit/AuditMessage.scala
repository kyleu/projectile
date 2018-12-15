package com.kyleu.projectile.models.project.audit

import com.kyleu.projectile.util.JsonSerializers._

object AuditMessage {
  implicit val jsonEncoder: Encoder[AuditMessage] = deriveEncoder
  implicit val jsonDecoder: Decoder[AuditMessage] = deriveDecoder
}

case class AuditMessage(
    srcModel: String,
    src: String,
    t: String,
    tgt: String,
    message: String
)
