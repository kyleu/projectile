package com.kyleu.projectile.controllers.admin.user

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.audit.AuditService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.models.web.ReftreeUtils._
import java.util.UUID

import com.kyleu.projectile.controllers.admin.user.routes.SystemUserController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.User.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.user.{SystemUser, SystemUserResult}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}
import com.kyleu.projectile.services.user.SystemUserService

import scala.util.control.NonFatal

@javax.inject.Singleton
class SystemUserController @javax.inject.Inject() (
    override val app: Application, svc: SystemUserService, noteSvc: NoteService, auditRecordSvc: AuditService, db: JdbcDatabase
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  ApplicationFeature.enable(ApplicationFeature.User)
  PermissionService.registerModel("models", "SystemUser", "System User", Some(InternalIcons.systemUser), "view", "edit")
  SystemMenu.addModelMenu(value, "System Users", Some("Manage the users of this application"), SystemUserController.list(), InternalIcons.systemUser)

  def createForm = withSession("create.form", ("models", "SystemUser", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list()
    val call = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.create()
    val cfg = app.cfg(u = Some(request.identity), "system", "models", "user", "Create")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.user.systemUserForm(
      cfg, SystemUser.empty(), "New System User", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("models", "SystemUser", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("models", "SystemUser", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id))
          case _ =>
            val cfg = app.cfg(u = Some(request.identity), "system", "models", "user")
            Ok(com.kyleu.projectile.views.html.admin.user.systemUserList(
              cfg, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)
            ))
        }
        case MimeTypes.JSON => Ok(SystemUserResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("SystemUser", svc.csvFor(r._1, r._2))
        case BaseController.MimeTypes.png => Ok(renderToPng(v = r._2)).as(BaseController.MimeTypes.png)
        case BaseController.MimeTypes.svg => Ok(renderToSvg(v = r._2)).as(BaseController.MimeTypes.svg)
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("models", "SystemUser", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("models", "SystemUser", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = noteSvc.getFor(request, "SystemUser", id).recover { case NonFatal(x) => Nil }
    val modelNotesF = noteSvc.getByAuthor(request, id, limit = Some(100)).recover { case NonFatal(x) => Nil }
    val auditsF = auditRecordSvc.getByModel(request, "SystemUser", id).recover { case NonFatal(x) => Nil }
    val modelAuditsF = auditRecordSvc.getByUserId(request, id, limit = Some(100)).recover { case NonFatal(x) => Nil }

    notesF.flatMap(notes => modelNotesF.flatMap(modelNotes => auditsF.flatMap(audits => modelAuditsF.flatMap(modelAudits => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(u = Some(request.identity), "system", "models", "user", model.id.toString)
          Ok(com.kyleu.projectile.views.html.admin.user.systemUserView(cfg, model, notes, modelNotes, audits, modelAudits, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
        case BaseController.MimeTypes.png => Ok(renderToPng(v = model)).as(BaseController.MimeTypes.png)
        case BaseController.MimeTypes.svg => Ok(renderToSvg(v = model)).as(BaseController.MimeTypes.svg)
      }
      case None => NotFound(s"No SystemUser found with id [$id]")
    }))))
  }

  def editForm(id: UUID) = withSession("edit.form", ("models", "SystemUser", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(id)
    val call = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) =>
        val cfg = app.cfg(u = Some(request.identity), "system", "models", "user", "Edit")
        Ok(com.kyleu.projectile.views.html.admin.user.systemUserForm(cfg, model, s"System User [$id]", cancel, call, debug = app.config.debug))
      case None => NotFound(s"No SystemUser found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("models", "SystemUser", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(res._1.id)).flashing("success" -> res._2)
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("models", "SystemUser", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}
