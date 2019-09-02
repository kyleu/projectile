// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers.admin.audit

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import java.util.UUID

import com.kyleu.projectile.controllers.admin.audit.routes.AuditController
import com.kyleu.projectile.models.audit.{Audit, AuditResult}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.result.RelationCount
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.audit.{AuditHelper, AuditRecordService, AuditService}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import javax.inject.Named
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuditController @javax.inject.Inject() (
    override val app: Application, svc: AuditService, recordSvc: AuditRecordService, noteSvc: NoteService, @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  ApplicationFeature.enable(ApplicationFeature.Audit)
  app.errors.checkTable("audit")
  PermissionService.registerModel("models", "Audit", "Audit", Some(InternalIcons.audit), "view", "edit")
  val desc = "System audits provide detailed change logging"
  SystemMenu.addModelMenu("audit", "Audits", Some(desc), AuditController.list(), InternalIcons.audit, ("models", "Audit", "view"))
  AuditHelper.init(appName = app.config.projectName, service = svc)

  def createForm = withSession("create.form", ("models", "Audit", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.audit.routes.AuditController.list()
    val call = com.kyleu.projectile.controllers.admin.audit.routes.AuditController.create()
    val cfg = app.cfg(u = Some(request.identity), "system", "models", "audit", "Create")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.audit.auditForm(
      cfg, Audit(act = "new"), "New Audit", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("models", "Audit", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("models", "Audit", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(model.id))
          case _ =>
            val cfg = app.cfg(u = Some(request.identity), "system", "models", "audit")
            Ok(com.kyleu.projectile.views.html.admin.audit.auditList(cfg, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(AuditResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("Audit", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("models", "Audit", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("models", "Audit", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val recordsF = recordSvc.getByAuditId(request, id)
    val notesF = noteSvc.getFor(request, "Audit", id)

    notesF.flatMap(notes => recordsF.flatMap(records => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(u = Some(request.identity), "system", "models", "audit", model.id.toString)
          Ok(com.kyleu.projectile.views.html.admin.audit.auditView(cfg, model, records, notes, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
      }
      case None => NotFound(s"No Audit found with id [$id]")
    }))
  }

  def editForm(id: UUID) = withSession("edit.form", ("models", "Audit", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(id)
    val call = com.kyleu.projectile.controllers.admin.audit.routes.AuditController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) =>
        val cfg = app.cfg(u = Some(request.identity), "system", "models", "audit", model.id.toString)
        Ok(com.kyleu.projectile.views.html.admin.audit.auditForm(cfg, model, s"Audit [$id]", cancel, call, debug = app.config.debug))
      case None => NotFound(s"No Audit found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("models", "Audit", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.view(res._1.id)).flashing("success" -> res._2)
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("models", "Audit", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }

  def relationCounts(id: UUID) = withSession("relation.counts", ("models", "Audit", "view")) { implicit request => implicit td =>
    val auditRecordByAuditIdF = recordSvc.countByAuditId(request, id)
    for (auditRecordByAuditIdC <- auditRecordByAuditIdF) yield {
      Ok(Seq(
        RelationCount(model = "auditRecord", field = "auditId", count = auditRecordByAuditIdC)
      ).asJson)
    }
  }
}
