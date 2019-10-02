package com.kyleu.projectile.models.audit

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.JsonSerializers._
import java.util.UUID

object AuditRecord {
  implicit val jsonEncoder: Encoder[AuditRecord] = deriveEncoder
  implicit val jsonDecoder: Decoder[AuditRecord] = deriveDecoder
}

final case class AuditRecord(
    id: UUID = UUID.randomUUID,
    auditId: UUID,
    t: String,
    pk: List[String] = Nil,
    changes: Seq[AuditField] = Nil
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("auditId", Some(auditId.toString)),
    DataField("t", Some(t)),
    DataField("pk", Some("{ " + pk.mkString(", ") + " }")),
    DataField("changes", Some(changes.asJson.noSpaces))
  )

  def toSummary = DataSummary(model = "AuditRecord", pk = id.toString, entries = Map("T" -> Some(t), "PK" -> Some(pk.mkString(", "))))
}
