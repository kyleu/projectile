package com.kyleu.projectile.models.audit

import com.google.inject.Injector
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.result.data.DataFieldModel
import com.kyleu.projectile.services.audit.AuditArgs.uuidArg
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
      log.debug("AuditCallbacks has not been initialized, and getByPk was called")
      Future.successful(None)
  }
  def getViewRoute(key: String, pk: IndexedSeq[String]): Option[Call] = key match {
    case "Audit" | "AuditRow" => Some(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(uuidArg(pk(0))))
    case "Note" | "NoteRow" => Some(com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(uuidArg(pk(0))))
    case "SystemUser" | "SystemUserRow" => Some(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(uuidArg(pk(0))))
    case _ => inst.map(_.getViewRoute(key, pk))
  }
}

trait AuditCallbackProvider {
  protected def injector: Injector

  def getByPk(creds: UserCredentials, model: String, pk: String*)(implicit traceData: TraceData): Future[Option[DataFieldModel]]
  def getViewRoute(key: String, pk: IndexedSeq[String]): Call
}
