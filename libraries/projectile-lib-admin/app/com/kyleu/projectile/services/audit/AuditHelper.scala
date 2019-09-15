package com.kyleu.projectile.services.audit

import java.net.InetAddress
import java.util.UUID

import com.kyleu.projectile.models.audit.{Audit, AuditField, AuditRecord}
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.module.ApplicationFeature
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.util.{Credentials, Logging}
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
    onAction("insert", t, pk, fields.map(f => AuditField(f.k, None, f.v)), creds)
  }

  def onUpdate(t: String, pk: Seq[String], origF: Seq[DataField], newF: Seq[DataField], creds: Credentials)(implicit trace: TraceData) = {
    onAction("update", t, pk, newF.flatMap(f => origF.find(_.k == f.k).flatMap {
      case o if f.v != o.v => Some(AuditField(f.k, o.v, f.v))
      case _ => None
    }), creds)
  }

  def onRemove(t: String, pk: Seq[String], fields: Seq[DataField], creds: Credentials)(implicit trace: TraceData) = {
    onAction("remove", t, pk, fields.map(f => AuditField(f.k, f.v, None)), creds)
  }

  def onAction(act: String, t: String, pk: Seq[String], changes: Seq[AuditField], creds: Credentials)(implicit trace: TraceData) = {
    val msg = s"Added audit of type [$act] for [$t] with [${changes.size}] changes:"
    val auditId = UUID.randomUUID
    val records = Seq(AuditRecord(auditId = auditId, t = t, pk = pk.toList, changes = changes))
    aud(creds, auditId, act, msg, records)
  }

  private[this] def getInfo(creds: Credentials) = creds match {
    case c: UserCredentials => c.remoteAddress -> Some(c.user.id)
    case _ => "unknown" -> None
  }

  private[this] def aud(creds: Credentials, id: UUID, act: String, msg: String, records: Seq[AuditRecord])(implicit trace: TraceData) = {
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      val (remoteAddress, userId) = getInfo(creds)
      userId match {
        case Some(u) =>
          onAudit(Audit(id = id, act = act, client = remoteAddress, server = server, userId = u, msg = msg), records.toList)
          true
        case None => false
      }
    } else {
      false
    }
  }
}
