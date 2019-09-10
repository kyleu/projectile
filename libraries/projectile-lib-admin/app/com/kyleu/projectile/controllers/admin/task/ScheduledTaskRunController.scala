// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers.admin.task

import java.util.UUID

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.task.{ScheduledTaskRun, ScheduledTaskRunResult}
import com.kyleu.projectile.services.audit.AuditService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.services.task.ScheduledTaskRunService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ScheduledTaskRunController @javax.inject.Inject() (
    override val app: Application, svc: ScheduledTaskRunService, noteSvc: NoteService, auditSvc: AuditService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  private[this] val defaultOrderBy = Some("started" -> false)

  def createForm = withSession("create.form", ("tools", "ScheduledTaskRun", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.list()
    val call = com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.create()
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.task.scheduledTaskRunForm(
      app.cfg(u = Some(request.identity), "system", "tools", "task", "Create"), ScheduledTaskRun.empty(),
      "New Scheduled Task Run", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("tools", "ScheduledTaskRun", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("tools", "ScheduledTaskRun", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.task.scheduledTaskRunList(
            cfg = app.cfg(u = Some(request.identity), "system", "tools", "task"),
            totalCount = Some(r._1),
            modelSeq = r._2,
            q = q,
            orderBy = orderBys.headOption.map(_.col),
            orderAsc = orderBys.exists(_.dir.asBool),
            limit = limit.getOrElse(100),
            offset = offset.getOrElse(0)
          ))
        }
        case MimeTypes.JSON => Ok(ScheduledTaskRunResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("ScheduledTaskRun", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("tools", "ScheduledTaskRun", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = {
    withSession("view", ("tools", "ScheduledTaskRun", "view")) { implicit request => implicit td =>
      val modelF = svc.getByPrimaryKey(request, id)
      val auditsF = auditSvc.getByModel(request, "ScheduledTaskRun", id)
      val notesF = noteSvc.getFor(request, "ScheduledTaskRun", id)

      notesF.flatMap(notes => auditsF.flatMap(audits => modelF.map {
        case Some(model) => renderChoice(t) {
          case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.task.scheduledTaskRunView(
            app.cfg(u = Some(request.identity), "system", "tools", "task", model.id.toString), model, notes, audits, app.config.debug
          ))
          case MimeTypes.JSON => Ok(model.asJson)
        }
        case None => NotFound(s"No ScheduledTaskRun found with id [$id]")
      }))
    }
  }

  def editForm(id: UUID) = withSession("edit.form", ("tools", "ScheduledTaskRun", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(id)
    val call = com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(com.kyleu.projectile.views.html.admin.task.scheduledTaskRunForm(
        cfg = app.cfg(u = Some(request.identity), "tools", "scheduled_task_run", "Edit"),
        model = model,
        title = s"Scheduled Task Run [$id]",
        cancel = cancel,
        act = call,
        debug = app.config.debug
      ))
      case None => NotFound(s"No ScheduledTaskRun found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("tools", "ScheduledTaskRun", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("tools", "ScheduledTaskRun", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.task.routes.ScheduledTaskRunController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}
