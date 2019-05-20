package com.kyleu.projectile.controllers.admin.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.services.audit.{AuditRecordService, AuditService}
import com.kyleu.projectile.services.search.SearchProvider
import com.kyleu.projectile.services.task.ScheduledTaskRunService
import com.kyleu.projectile.services.user.SystemUserService
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SearchController @javax.inject.Inject() (
    override val app: Application, injector: Injector, provider: SearchProvider
)(implicit ec: ExecutionContext) extends AuthController("search") {
  ApplicationFeatures.enable("search")

  def search(q: String) = withSession("admin.search", admin = true) { implicit request => implicit td =>
    val t = q.trim
    val creds = UserCredentials.fromRequest(request)
    val results = try {
      searchInt(creds, t, t.toInt)
    } catch {
      case _: NumberFormatException => try {
        searchUuid(creds, t, UUID.fromString(t))
      } catch {
        case _: IllegalArgumentException => searchString(creds, t)
      }
    }
    results.map {
      case r if r.size == 1 => Redirect(r.head._1)
      case r =>
        val cfg = app.cfgAdmin(u = request.identity, "Search", q)
        Ok(com.kyleu.projectile.views.html.admin.explore.searchResults(q, r.map(_._2), cfg))
    }
  }

  private[this] def searchInt(creds: UserCredentials, q: String, id: Int)(implicit timing: TraceData) = {
    val intSearches = provider.intSearches(app, injector, creds)(q, id)(ec, timing)
    Future.sequence(intSearches).map(_.flatten)
  }

  private[this] def searchUuid(creds: UserCredentials, q: String, id: UUID)(implicit timing: TraceData) = {
    val uuidSearches = provider.uuidSearches(app, injector, creds)(q, id)(ec, timing) ++ List(
      if (ApplicationFeatures.enabled("audit")) { Seq(injector.getInstance(classOf[AuditRecordService]).getByPrimaryKey(creds, id).map(_.map(model => com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id) -> com.kyleu.projectile.views.html.admin.audit.auditRecordSearchResult(model, s"Audit Record [${model.id}] matched [$q]")).toSeq)) } else { Nil },
      if (ApplicationFeatures.enabled("audit")) { Seq(injector.getInstance(classOf[AuditService]).getByPrimaryKey(creds, id).map(_.map(model => com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id) -> com.kyleu.projectile.views.html.admin.audit.auditSearchResult(model, s"Audit [${model.id}] matched [$q]")).toSeq)) } else { Nil },
      if (ApplicationFeatures.enabled("user")) { Seq(injector.getInstance(classOf[SystemUserService]).getByPrimaryKey(creds, id).map(_.map(model => com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id) -> com.kyleu.projectile.views.html.admin.user.systemUserSearchResult(model, s"System User [${model.id}] matched [$q]")).toSeq)) } else { Nil },
      if (ApplicationFeatures.enabled("task")) { Seq(injector.getInstance(classOf[ScheduledTaskRunService]).getByPrimaryKey(creds, id).map(_.map(model => com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id) -> com.kyleu.projectile.views.html.admin.task.scheduledTaskRunSearchResult(model, s"Scheduled Task Run [${model.id}] matched [$q]")).toSeq)) } else { Nil }
    ).flatten

    Future.sequence(uuidSearches).map(_.flatten)
  }

  private[this] def searchString(creds: UserCredentials, q: String)(implicit timing: TraceData) = {
    val stringSearches = provider.stringSearches(app, injector, creds)(q)(ec, timing) ++ List(
      if (ApplicationFeatures.enabled("audit")) { Seq(injector.getInstance(classOf[AuditRecordService]).searchExact(creds, q = q, limit = Some(5)).map(_.map(model => com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id) -> com.kyleu.projectile.views.html.admin.audit.auditRecordSearchResult(model, s"Audit Record [${model.id}] matched [$q]")))) } else { Nil },
      if (ApplicationFeatures.enabled("audit")) { Seq(injector.getInstance(classOf[AuditService]).searchExact(creds, q = q, limit = Some(5)).map(_.map(model => com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id) -> com.kyleu.projectile.views.html.admin.audit.auditSearchResult(model, s"Audit [${model.id}] matched [$q]")))) } else { Nil },
      if (ApplicationFeatures.enabled("user")) { Seq(injector.getInstance(classOf[SystemUserService]).searchExact(creds, q = q, limit = Some(5)).map(_.map(model => com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id) -> com.kyleu.projectile.views.html.admin.user.systemUserSearchResult(model, s"System User [${model.id}] matched [$q]")))) } else { Nil },
      if (ApplicationFeatures.enabled("task")) { Seq(injector.getInstance(classOf[ScheduledTaskRunService]).searchExact(creds, q = q, limit = Some(5)).map(_.map(model => com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id) -> com.kyleu.projectile.views.html.admin.task.scheduledTaskRunSearchResult(model, s"Scheduled Task Run [${model.id}] matched [$q]")))) } else { Nil }
    ).flatten
    Future.sequence(stringSearches).map(_.flatten)
  }
}
