package com.kyleu.projectile.controllers.admin.reporting

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.controllers.{AuthController, BaseController}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.reporting.ReportResult
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.reporting.ReportService
import com.kyleu.projectile.util.CsvUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.tracing.TraceData
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ReportController @javax.inject.Inject() (
    override val app: Application, injector: Injector
)(implicit ec: ExecutionContext) extends AuthController("reporting") {
  ApplicationFeature.enable(ApplicationFeature.Reporting)
  PermissionService.registerModel("tools", "Reporting", "Reporting", Some(InternalIcons.reporting), "view", "run")
  SystemMenu.addToolMenu(
    key = ApplicationFeature.Reporting.value,
    title = "Reporting",
    desc = Some("Run the reports that have been configured"),
    call = com.kyleu.projectile.controllers.admin.reporting.routes.ReportController.list(),
    icon = InternalIcons.reporting,
    ("tools", "Reporting", "view")
  )

  def list = withSession("list", ("tools", "Reporting", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "reporting")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.reporting.reportList(cfg)))
  }

  def run(key: String, t: Option[String]) = withSession(key, ("tools", "Reporting", "run")) { implicit request => implicit td =>
    val args = request.queryString.map(x => x._1 -> x._2.headOption.getOrElse("")).filter {
      case x if x._1 == "t" => false
      case x if x._1 == "sort" => false
      case _ => true
    }
    ReportService.run(key, args.toMap, request.identity.id, request.identity.role, injector).map { result =>
      // Redirect(com.kyleu.projectile.controllers.admin.reporting.routes.ReportController.cached(result.id))
      response(result, t)
    }
  }

  def cached(id: UUID, t: Option[String], sort: Option[String]) = {
    withSession("cached", ("tools", "Reporting", "run")) { implicit request => implicit td =>
      val result = ReportService.getCachedResult(id, request.identity.id, request.identity.role).getOrElse {
        throw new IllegalStateException(s"No report result found with id [$id]")
      }
      Future.successful(response(result.sortBy(sort), t, sort))
    }
  }

  def response(result: ReportResult, t: Option[String], sort: Option[String] = None)(implicit request: Req, td: TraceData) = {
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "reporting", result.report.title)
    Ok(com.kyleu.projectile.views.html.admin.reporting.reportRun(cfg, result))
    renderChoice(t) {
      case MimeTypes.HTML =>
        val cfg = app.cfg(u = Some(request.identity), "system", "tools", "reporting", result.report.title)
        Ok(com.kyleu.projectile.views.html.admin.reporting.reportRun(cfg, result.sortBy(sort), sort))
      case MimeTypes.JSON => Ok(result.asJson)
      case BaseController.MimeTypes.csv => csvResponse(result.report.title, csvFor(result))
    }
  }

  private[this] def csvFor(result: ReportResult)(implicit trace: TraceData) = {
    CsvUtils.csvForRows(Some(result.report.title), result.rows.size, result.rows.map(_.map(_.map(_._1))), result.columns.map(_._1))(trace)
  }
}
