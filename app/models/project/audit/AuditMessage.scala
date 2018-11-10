package models.project.audit

import util.JsonSerializers._

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
