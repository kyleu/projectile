package com.kyleu.projectile.models.audit

import com.google.inject.Injector
import com.kyleu.projectile.services.audit.AuditArgs.{longArg, uuidArg}
import com.kyleu.projectile.util.Logging
import play.api.mvc.Call

object AuditCallbackProvider extends Logging {
  private[this] var inst: Option[AuditCallbackProvider] = None

  def init(callbacks: AuditCallbackProvider) = {
    inst = Some(callbacks)
  }

  def getViewRoute(key: String, pk: IndexedSeq[String]): Option[Call] = key match {
    case "Audit" => Some(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(uuidArg(pk(0))))
    case "AuditRecord" => Some(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(uuidArg(pk(0))))
    case "DatabaseMigration" => Some(com.kyleu.projectile.controllers.admin.migrate.routes.MigrationController.view(longArg(pk(0))))
    case "Note" => Some(com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(uuidArg(pk(0))))
    case "ScheduledTaskRun" => Some(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(uuidArg(pk(0))))
    case "SystemUser" => Some(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(uuidArg(pk(0))))
    case _ => inst.map(_.getViewRoute(key, pk))
  }
}

trait AuditCallbackProvider {
  protected def injector: Injector

  def getViewRoute(key: String, pk: IndexedSeq[String]): Call
}
