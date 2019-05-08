package com.kyleu.projectile.controllers.admin.user

import com.kyleu.projectile.controllers.{ServiceAuthController, ServiceController}
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.audit.AuditService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.models.web.ReftreeUtils._
import java.util.UUID
import com.kyleu.projectile.models.user.{SystemUser, SystemUserResult}
import play.api.http.MimeTypes
import scala.concurrent.{ExecutionContext, Future}
import com.kyleu.projectile.services.user.SystemUserService

@javax.inject.Singleton
class SystemUserController @javax.inject.Inject() (
    override val app: Application, svc: SystemUserService, noteSvc: NoteService, auditRecordSvc: AuditService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {

  def createForm = withSession("create.form", admin = true) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list()
    val call = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.create()
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.user.systemUserForm(
      app.cfg(Some(request.identity), true, "auth", "system_user", "Create"), SystemUser.empty(), "New System User", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", admin = true) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", admin = true) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.user.systemUserList(app.cfg(u = Some(request.identity), admin = true, "auth", "system_user"), Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(SystemUserResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case ServiceController.MimeTypes.csv => csvResponse("SystemUser", svc.csvFor(r._1, r._2))
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

  def view(id: UUID, t: Option[String] = None) = withSession("view", admin = true) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val auditsF = auditRecordSvc.getByModel(request, "SystemUser", id)
    val notesF = noteSvc.getFor(request, "SystemUser", id)

    notesF.flatMap(notes => auditsF.flatMap(audits => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.user.systemUserView(app.cfg(Some(request.identity), true, "auth", "system_user", model.id.toString), model, notes, audits, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
        case ServiceController.MimeTypes.png => Ok(renderToPng(v = model)).as(ServiceController.MimeTypes.png)
        case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = model)).as(ServiceController.MimeTypes.svg)
      }
      case None => NotFound(s"No SystemUser found with id [$id]")
    }))
  }

  def editForm(id: UUID) = withSession("edit.form", admin = true) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(id)
    val call = com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        com.kyleu.projectile.views.html.admin.user.systemUserForm(app.cfg(Some(request.identity), true, "auth", "system_user", "Edit"), model, s"System User [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No SystemUser found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", admin = true) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.view(res._1.id)).flashing("success" -> res._2)
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", admin = true) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}
