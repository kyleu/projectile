package com.kyleu.projectile.controllers.admin.search

import com.google.inject.Injector
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.module.ApplicationFeature
import com.kyleu.projectile.services.audit.{AuditRecordService, AuditService}
import com.kyleu.projectile.services.feedback.FeedbackService
import com.kyleu.projectile.services.task.ScheduledTaskRunService
import com.kyleu.projectile.services.user.SystemUserService
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

object InternalStringSearchHelpers {
  def string(q: String, injector: Injector, creds: UserCredentials)(implicit td: TraceData, ec: ExecutionContext) = Seq(
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      val svc = injector.getInstance(classOf[AuditRecordService])
      Seq(svc.searchExact(creds, q, limit = Some(5)).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.audit.auditRecordSearchResult(model, s"Audit Record [${model.id}] matched [$q]")
        com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id) -> r
      }))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      val svc = injector.getInstance(classOf[AuditService])
      Seq(svc.searchExact(creds, q, limit = Some(5)).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.audit.auditSearchResult(model, s"Audit [${model.id}] matched [$q]")
        com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id) -> r
      }))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Feedback)) {
      val svc = injector.getInstance(classOf[FeedbackService])
      Seq(svc.searchExact(creds, q, limit = Some(5)).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.feedback.feedbackSearchResult(model, s"Feedback [${model.id}] matched [$q]")
        com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(model.id) -> r
      }))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.User)) {
      val svc = injector.getInstance(classOf[SystemUserService])
      Seq(svc.searchExact(creds, q, limit = Some(5)).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.user.systemUserSearchResult(model, s"System User [${model.id}] matched [$q]")
        com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id) -> r
      }))
    } else { Nil },
    if (ApplicationFeature.enabled(ApplicationFeature.Task)) {
      val svc = injector.getInstance(classOf[ScheduledTaskRunService])
      Seq(svc.searchExact(creds, q, limit = Some(5)).map(_.map { model =>
        val r = com.kyleu.projectile.views.html.admin.task.scheduledTaskRunSearchResult(model, s"Scheduled Task Run [${model.id}] matched [$q]")
        com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id) -> r
      }))
    } else { Nil }
  )
}
