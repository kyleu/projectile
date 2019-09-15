package com.kyleu.projectile.controllers.admin.error

import java.util.UUID

import com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController
import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.error.{SystemError, SystemErrorResult}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.{ControllerUtils, InternalIcons}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.error.SystemErrorService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import play.api.http.MimeTypes
import com.kyleu.projectile.views.html.layout.{card, page}

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SystemErrorController @javax.inject.Inject() (
    override val app: Application, svc: SystemErrorService, noteSvc: NoteService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  ApplicationFeature.enable(ApplicationFeature.Error)
  app.errors.checkTable("system_error")

  PermissionService.registerModel("error", "SystemError", "System Error", Some(InternalIcons.error), "view")
  val desc = "Manage the users of this application"
  SystemMenu.addModelMenu(ApplicationFeature.Error.value, "System Errors", Some(desc), SystemErrorController.list(), InternalIcons.error, ("models", "SystemError", "view"))

  private[this] val defaultOrderBy = Some("occurred" -> false)

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("models", "SystemError", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.error.systemErrorList(app.cfg(u = Some(request.identity), "system", "models", "error"), Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(SystemErrorResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("SystemError", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("models", "SystemError", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("models", "SystemError", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = noteSvc.getFor(request, "SystemError", id)

    notesF.flatMap(notes => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.error.systemErrorView(app.cfg(u = Some(request.identity), "system", "models", "error", model.id.toString), model, notes, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
      }
      case None => NotFound(s"No SystemError found with id [$id]")
    })
  }

  def editForm(id: UUID) = withSession("edit.form", ("models", "SystemError", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.view(id)
    val call = com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        com.kyleu.projectile.views.html.admin.error.systemErrorForm(app.cfg(Some(request.identity), "system", "models", "error", "Edit"), model, s"System Error [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No SystemError found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("models", "SystemError", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("models", "SystemError", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
  def createForm = withSession("create.form", ("models", "SystemError", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.list()
    val call = com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.create()
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.error.systemErrorForm(
      app.cfg(u = Some(request.identity), "system", "models", "error", "Create"), SystemError.empty(), "New System Error", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("models", "SystemError", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.list())
    }
  }

  def bulkEdit = withSession("bulk.edit", ("user", "UserAuthTokenRow", "edit")) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val pks = form("primaryKeys").split("//").map(_.trim).filter(_.nonEmpty).map(_.split("---").map(_.trim).filter(_.nonEmpty).toList).toList
    val changes = modelForm(request.body)
    svc.updateBulk(request, pks, changes).map(msg => Ok("OK: " + msg))
  }

  def byUserId(userId: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false) = {
    withSession("get.by.userId", ("user", "UserAuthTokenRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      svc.getByUserId(request, userId, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(Some(request.identity), "user", "user_auth_token", "User Id")
          val list = com.kyleu.projectile.views.html.admin.error.systemErrorByUserId(cfg, userId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0))
          if (embedded) { Ok(list) } else { Ok(page(s"User Auth Tokens by User Id [$userId]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case BaseController.MimeTypes.csv => csvResponse("UserAuthTokenRow by userId", svc.csvFor(0, models))
      })
    }
  }

  def byUserIdBulkForm(userId: UUID) = {
    withSession("get.by.userId", ("user", "UserAuthTokenRow", "edit")) { implicit request => implicit td =>
      svc.getByUserId(request, userId).map { modelSeq =>
        val act = com.kyleu.projectile.controllers.admin.error.routes.SystemErrorController.bulkEdit()
        Ok(com.kyleu.projectile.views.html.admin.error.systemErrorBulkForm(app.cfg(Some(request.identity), "error", "system_error", "Bulk Edit"), modelSeq, act, debug = app.config.debug))
      }
    }
  }
}
