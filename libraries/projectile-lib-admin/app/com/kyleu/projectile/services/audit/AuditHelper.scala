package com.kyleu.projectile.services.audit

import java.net.InetAddress
import java.util.UUID

import com.kyleu.projectile.models.audit.{Audit, AuditField, AuditRecord}
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

object AuditHelper extends Logging {
  private[this] lazy val server = InetAddress.getLocalHost.getHostName
  private[this] var inst: Option[(String, AuditService)] = None

  def init(appName: String, service: AuditService) = {
    inst = Some(appName -> service)
  }

  def onAudit(audit: Audit, records: Seq[AuditRecord])(implicit trace: TraceData) = inst match {
    case Some(i) => i._2.callback(audit.copy(app = i._1), records)
    case None => log.info("Audit attempted without initialized AuditService")
  }

  def onInsert(t: String, pk: Seq[String], fields: Seq[DataField], creds: Credentials)(implicit trace: TraceData) = {
    val msg = s"Inserted new [$t] with [${fields.size}] fields:"
    val auditId = UUID.randomUUID
    val records = Seq(AuditRecord(auditId = auditId, t = t, pk = pk.toList, changes = fields.map(f => AuditField(f.k, None, f.v))))
    aud(creds, auditId, "insert", msg, records)
  }

  def onUpdate(
    t: String, pk: Seq[String], originalFields: Seq[DataField], newFields: Seq[DataField], creds: Credentials
  )(implicit trace: TraceData) = {
    def changeFor(f: DataField) = originalFields.find(_.k == f.k).flatMap {
      case o if f.v != o.v => Some(AuditField(f.k, o.v, f.v))
      case _ => None
    }
    val changes = newFields.flatMap(changeFor)
    val msg = s"Updated [${changes.size}] fields of $t[${pk.mkString(", ")}]"
    val auditId = UUID.randomUUID
    val records = Seq(AuditRecord(auditId = auditId, t = t, pk = pk.toList, changes = changes))
    aud(creds, auditId, "update", msg, records)
  }

  def onRemove(t: String, pk: Seq[String], fields: Seq[DataField], creds: Credentials)(implicit trace: TraceData) = {
    val msg = s"Removed [$t] with [${fields.size}] fields:"
    val auditId = UUID.randomUUID
    val records = Seq(AuditRecord(auditId = auditId, t = t, pk = pk.toList, changes = fields.map(f => AuditField(f.k, None, f.v))))
    aud(creds, auditId, "remove", msg, records)
  }

  private[this] def getInfo(creds: Credentials) = creds match {
    case c: UserCredentials => c.remoteAddress -> Some(c.user.id)
    case _ => "unknown" -> None
  }

  private[this] def aud(creds: Credentials, id: UUID, act: String, msg: String, records: Seq[AuditRecord])(implicit trace: TraceData) = {
    val (remoteAddress, userId) = getInfo(creds)
    onAudit(Audit(id = id, act = act, client = remoteAddress, server = server, userId = userId, msg = msg), records.toList)
  }
}
