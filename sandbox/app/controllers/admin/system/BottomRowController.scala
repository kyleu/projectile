/* Generated File */
package controllers.admin.system

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.ControllerUtils
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.views.html.layout.{card, page}
import java.util.UUID
import models.{BottomRow, BottomRowResult}
import play.api.http.MimeTypes
import scala.concurrent.{ExecutionContext, Future}
import services.BottomRowService

@javax.inject.Singleton
class BottomRowController @javax.inject.Inject() (
    override val app: Application, svc: BottomRowService, noteSvc: NoteService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  PermissionService.registerModel("system", "BottomRow", "Bottom", Some(models.template.Icons.bottomRow), "view", "edit")
  private[this] val defaultOrderBy = None

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("view", ("system", "BottomRow", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(controllers.admin.system.routes.BottomRowController.view(model.id))
          case _ => Ok(views.html.admin.bottomRowList(app.cfg(u = Some(request.identity), "system", "bottom"), Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(BottomRowResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("BottomRow", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("system", "BottomRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("system", "BottomRow", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = noteSvc.getFor(request, "BottomRow", id)

    notesF.flatMap(notes => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(views.html.admin.bottomRowView(app.cfg(u = Some(request.identity), "system", "bottom", model.id.toString), model, notes, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
      }
      case None => NotFound(s"No BottomRow found with id [$id]")
    })
  }

  def editForm(id: UUID) = withSession("edit.form", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.system.routes.BottomRowController.view(id)
    val call = controllers.admin.system.routes.BottomRowController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        views.html.admin.bottomRowForm(app.cfg(Some(request.identity), "system", "bottom", "Edit"), model, s"Bottom [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No BottomRow found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(controllers.admin.system.routes.BottomRowController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(controllers.admin.system.routes.BottomRowController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
  def createForm = withSession("create.form", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.system.routes.BottomRowController.list()
    val call = controllers.admin.system.routes.BottomRowController.create()
    Future.successful(Ok(views.html.admin.bottomRowForm(
      app.cfg(u = Some(request.identity), "system", "bottom", "Create"), BottomRow.empty(), "New Bottom", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(controllers.admin.system.routes.BottomRowController.view(model.id))
      case None => Redirect(controllers.admin.system.routes.BottomRowController.list())
    }
  }

  def bulkEdit = withSession("bulk.edit", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val pks = form("primaryKeys").split("//").map(_.trim).filter(_.nonEmpty).map(_.split("---").map(_.trim).filter(_.nonEmpty).toList).toList
    val changes = modelForm(request.body)
    svc.updateBulk(request, pks, changes).map(msg => Ok("OK: " + msg))
  }

  def byTopId(topId: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false) = {
    withSession("get.by.topId", ("system", "BottomRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      svc.getByTopId(request, topId, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(Some(request.identity), "system", "bottom", "Top Id")
          val list = views.html.admin.bottomRowByTopId(cfg, topId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0))
          if (embedded) { Ok(list) } else { Ok(page(s"Bottoms by Top Id [$topId]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case BaseController.MimeTypes.csv => csvResponse("BottomRow by topId", svc.csvFor(0, models))
      })
    }
  }

  def byTopIdBulkForm(topId: UUID) = {
    withSession("get.by.topId", ("system", "BottomRow", "edit")) { implicit request => implicit td =>
      svc.getByTopId(request, topId).map { modelSeq =>
        val act = controllers.admin.system.routes.BottomRowController.bulkEdit()
        Ok(views.html.admin.bottomRowBulkForm(app.cfg(Some(request.identity), "system", "bottom", "Bulk Edit"), modelSeq, act, debug = app.config.debug))
      }
    }
  }
}
