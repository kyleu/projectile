package com.kyleu.projectile.controllers.admin.audit

import com.kyleu.projectile.views.html.layout.{card, page}
import com.kyleu.projectile.controllers.{ServiceAuthController, ServiceController}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.models.web.ReftreeUtils._
import java.util.UUID

import com.kyleu.projectile.models.audit.{AuditRecord, AuditRecordResult}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.audit.AuditRecordService
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuditRecordController @javax.inject.Inject() (
    override val app: Application, svc: AuditRecordService, noteSvc: NoteService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {

  def createForm = withSession("create.form", admin = true) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.list()
    val call = com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.create()
    val model = AuditRecord(auditId = UUID.randomUUID, t = "")
    val cfg = app.cfgAdmin(u = request.identity, "audit", "Record", "Create")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.audit.auditRecordForm(
      cfg, model, "New Audit Record", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", admin = true) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", admin = true) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.audit.auditRecordList(app.cfgAdmin(u = request.identity, "audit", "Record"), Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(AuditRecordResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case ServiceController.MimeTypes.csv => csvResponse("AuditRecord", svc.csvFor(r._1, r._2))
        case ServiceController.MimeTypes.png => Ok(renderToPng(v = r._2)).as(ServiceController.MimeTypes.png)
        case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = r._2)).as(ServiceController.MimeTypes.svg)
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", admin = true) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def byAuditId(auditId: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false) = {
    withSession("get.by.auditId", admin = true) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      svc.getByAuditId(request, auditId, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfgAdmin(u = request.identity, "audit", "Record", "Audit Id")
          val list = com.kyleu.projectile.views.html.admin.audit.auditRecordByAuditId(cfg, auditId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0))
          if (embedded) { Ok(list) } else { Ok(page(s"Audit Records by Audit Id [$auditId]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case ServiceController.MimeTypes.csv => csvResponse("AuditRecord by auditId", svc.csvFor(0, models))
        case ServiceController.MimeTypes.png => Ok(renderToPng(v = models)).as(ServiceController.MimeTypes.png)
        case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = models)).as(ServiceController.MimeTypes.svg)
      })
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", admin = true) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = noteSvc.getFor(request, "AuditRecord", id)

    notesF.flatMap(notes => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.audit.auditRecordView(app.cfgAdmin(u = request.identity, "audit", "Record", model.id.toString), model, notes, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
        case ServiceController.MimeTypes.png => Ok(renderToPng(v = model)).as(ServiceController.MimeTypes.png)
        case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = model)).as(ServiceController.MimeTypes.svg)
      }
      case None => NotFound(s"No AuditRecord found with id [$id]")
    })
  }

  def editForm(id: UUID) = withSession("edit.form", admin = true) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(id)
    val call = com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        com.kyleu.projectile.views.html.admin.audit.auditRecordForm(app.cfgAdmin(u = request.identity, "audit", "Record", "Edit"), model, s"Audit Record [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No AuditRecord found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", admin = true) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.view(res._1.id)).flashing("success" -> res._2)
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", admin = true) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditRecordController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}