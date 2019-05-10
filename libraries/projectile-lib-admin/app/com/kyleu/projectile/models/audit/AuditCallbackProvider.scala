package com.kyleu.projectile.models.audit

import com.google.inject.Injector
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.result.data.DataFieldModel
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.Call

import scala.concurrent.Future

object AuditCallbackProvider extends Logging {
  private[this] var inst: Option[AuditCallbackProvider] = None

  def init(callbacks: AuditCallbackProvider) = {
    inst = Some(callbacks)
  }

  def getByPk(creds: UserCredentials, model: String, pk: String*)(implicit traceData: TraceData): Future[Option[DataFieldModel]] = inst match {
    case Some(i) => i.getByPk(creds, model, pk: _*)
    case None =>
      log.warn("AuditCallbacks has not been initialized, and getByPk was called")
      Future.successful(None)
  }
  def getViewRoute(key: String, pk: IndexedSeq[String]): Option[Call] = inst match {
    case Some(i) => Some(i.getViewRoute(key, pk))
    case None =>
      log.warn("AuditCallbacks has not been initialized, and getViewRoute was called")(TraceData.noop)
      None
  }
}

trait AuditCallbackProvider {
  protected def injector: Injector

  def getByPk(creds: UserCredentials, model: String, pk: String*)(implicit traceData: TraceData): Future[Option[DataFieldModel]]
  def getViewRoute(key: String, pk: IndexedSeq[String]): Call
}
