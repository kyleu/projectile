/* Generated File */
package controllers.admin.size

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.ControllerUtils
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.{Credentials, DateUtils}
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.views.html.layout.{card, page}
import models.size.{SmallRow, SmallRowResult}
import play.api.http.MimeTypes
import scala.concurrent.{ExecutionContext, Future}
import services.size.{BigRowService, SmallRowService}

@javax.inject.Singleton
class SmallRowController @javax.inject.Inject() (
    override val app: Application, svc: SmallRowService, noteSvc: NoteService,
    bigRowS: BigRowService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  PermissionService.registerModel("size", "SmallRow", "Small", Some(models.template.Icons.smallRow), "view", "edit")
  private[this] val defaultOrderBy = None

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("size", "SmallRow", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(controllers.admin.size.routes.SmallRowController.view(model.id))
          case _ => Ok(views.html.admin.size.smallRowList(app.cfg(u = Some(request.identity), "size", "small"), Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(SmallRowResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("SmallRow", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("size", "SmallRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: Long, t: Option[String] = None) = withSession("view", ("size", "SmallRow", "view")) { implicit request => implicit td =>
    val creds: Credentials = request
    val modelF = svc.getByPrimaryKeyRequired(creds, id)
    val notesF = noteSvc.getFor(creds, "SmallRow", id)
    val bigIdF = modelF.flatMap(m => bigRowS.getByPrimaryKey(creds, m.bigId))

    bigIdF.flatMap(bigIdR =>
      notesF.flatMap(notes => modelF.map { model =>
        renderChoice(t) {
          case MimeTypes.HTML => Ok(views.html.admin.size.smallRowView(app.cfg(u = Some(request.identity), "size", "small", model.id.toString), model, notes, bigIdR, app.config.debug))
          case MimeTypes.JSON => Ok(model.asJson)
        }
      }))
  }

  def editForm(id: Long) = withSession("edit.form", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.size.routes.SmallRowController.view(id)
    val call = controllers.admin.size.routes.SmallRowController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        views.html.admin.size.smallRowForm(app.cfg(Some(request.identity), "size", "small", "Edit"), model, s"Small [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No SmallRow found with id [$id]")
    }
  }

  def edit(id: Long) = withSession("edit", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(controllers.admin.size.routes.SmallRowController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: Long) = withSession("remove", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(controllers.admin.size.routes.SmallRowController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
  def createForm = withSession("create.form", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.size.routes.SmallRowController.list()
    val call = controllers.admin.size.routes.SmallRowController.create()
    Future.successful(Ok(views.html.admin.size.smallRowForm(
      app.cfg(u = Some(request.identity), "size", "small", "Create"), SmallRow.empty(), "New Small", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(controllers.admin.size.routes.SmallRowController.view(model.id))
      case None => Redirect(controllers.admin.size.routes.SmallRowController.list())
    }
  }

  def bulkEditForm = withSession("bulk.edit.form", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    val act = controllers.admin.size.routes.SmallRowController.bulkEdit()
    Future.successful(Ok(views.html.admin.size.smallRowBulkForm(app.cfg(Some(request.identity), "size", "small", "Bulk Edit"), Nil, act, debug = app.config.debug)))
  }
  def bulkEdit = withSession("bulk.edit", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val pks = form("primaryKeys").split("//").map(_.trim).filter(_.nonEmpty).map(_.split("---").map(_.trim).filter(_.nonEmpty).toList).toList
    val typed = pks.map(pk => pk.headOption.getOrElse(throw new IllegalStateException()).toLong)
    val changes = modelForm(request.body)
    svc.updateBulk(request, typed, changes).map(msg => Ok("OK: " + msg))
  }

  def byBigId(bigId: Long, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false) = {
    withSession("get.by.bigId", ("size", "SmallRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      svc.getByBigId(request, bigId, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(Some(request.identity), "size", "small", "Big Id")
          val list = views.html.admin.size.smallRowByBigId(cfg, bigId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0))
          if (embedded) { Ok(list) } else { Ok(page(s"Smalls by Big Id [$bigId]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case BaseController.MimeTypes.csv => csvResponse("SmallRow by bigId", svc.csvFor(0, models))
      })
    }
  }

  def byBigIdBulkForm(bigId: Long) = {
    withSession("get.by.bigId", ("size", "SmallRow", "edit")) { implicit request => implicit td =>
      svc.getByBigId(request, bigId).map { modelSeq =>
        val act = controllers.admin.size.routes.SmallRowController.bulkEdit()
        Ok(views.html.admin.size.smallRowBulkForm(app.cfg(Some(request.identity), "size", "small", "Bulk Edit"), modelSeq, act, debug = app.config.debug))
      }
    }
  }
}
