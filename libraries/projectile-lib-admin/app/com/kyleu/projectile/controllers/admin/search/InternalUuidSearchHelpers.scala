package com.kyleu.projectile.controllers.admin.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.module.ApplicationFeature
import com.kyleu.projectile.services.audit.{AuditRecordService, AuditService}
import com.kyleu.projectile.services.feedback.FeedbackService
import com.kyleu.projectile.services.task.ScheduledTaskRunService
import com.kyleu.projectile.services.user.SystemUserService
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

object InternalUuidSearchHelpers {
  def uuid(q: String, id: UUID, injector: Injector, creds: UserCredentials)(implicit td: TraceData, ec: ExecutionContext) = Seq(
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      Seq(injector.getInstance(classOf[AuditRecordService]).getByPrimaryKey(creds, id).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.audit.auditRecordSearchResult(model, s"Audit Record [${model.id}] matched id [$q]")
        com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id) -> r
      }.toSeq))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      Seq(injector.getInstance(classOf[AuditService]).getByPrimaryKey(creds, id).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.audit.auditSearchResult(model, s"Audit [${model.id}] matched id [$q]")
        com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id) -> r
      }.toSeq))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Feedback)) {
      Seq(injector.getInstance(classOf[FeedbackService]).getByPrimaryKey(creds, id).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.feedback.feedbackSearchResult(model, s"Feedback [${model.id}] matched id [$q]")
        com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(model.id) -> r
      }.toSeq))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.User)) {
      Seq(injector.getInstance(classOf[SystemUserService]).getByPrimaryKey(creds, id).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.user.systemUserSearchResult(model, s"System User [${model.id}] matched id [$q]")
        com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id) -> r
      }.toSeq))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Task)) {
      Seq(injector.getInstance(classOf[ScheduledTaskRunService]).getByPrimaryKey(creds, id).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.task.scheduledTaskRunSearchResult(model, s"Scheduled Task Run [${model.id}] matched id [$q]")
        com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id) -> r
      }.toSeq))
    } else { Nil }
  )
}
