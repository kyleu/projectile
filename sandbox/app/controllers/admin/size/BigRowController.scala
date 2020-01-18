/* Generated File */
package controllers.admin.size

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.RelationCount
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.ControllerUtils
import com.kyleu.projectile.services.audit.AuditService
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.{Credentials, DateUtils}
import com.kyleu.projectile.util.JsonSerializers._
import models.size.{BigRow, BigRowResult}
import play.api.http.MimeTypes
import scala.concurrent.{ExecutionContext, Future}
import services.size.{BigRowService, SmallRowService}

@javax.inject.Singleton
class BigRowController @javax.inject.Inject() (
    override val app: Application, svc: BigRowService, noteSvc: NoteService, auditSvc: AuditService,
    smallRowS: SmallRowService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  PermissionService.registerModel("size", "BigRow", "Big", Some(models.template.Icons.bigRow), "view", "edit")
  private[this] val defaultOrderBy = None

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("size", "BigRow", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(controllers.admin.size.routes.BigRowController.view(model.id))
          case _ => Ok(views.html.admin.size.bigRowList(app.cfg(u = Some(request.identity), "size", "big"), Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(BigRowResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("BigRow", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("size", "BigRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: Long, t: Option[String] = None) = withSession("view", ("size", "BigRow", "view")) { implicit request => implicit td =>
    val creds: Credentials = request
    val modelF = svc.getByPrimaryKeyRequired(creds, id)
    val auditsF = auditSvc.getByModel(creds, "BigRow", id)
    val notesF = noteSvc.getFor(creds, "BigRow", id)

    notesF.flatMap(notes => auditsF.flatMap(audits => modelF.map { model =>
      renderChoice(t) {
        case MimeTypes.HTML => Ok(views.html.admin.size.bigRowView(app.cfg(u = Some(request.identity), "size", "big", model.id.toString), model, notes, audits, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
      }
    }))
  }

  def editForm(id: Long) = withSession("edit.form", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.size.routes.BigRowController.view(id)
    val call = controllers.admin.size.routes.BigRowController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        views.html.admin.size.bigRowForm(app.cfg(Some(request.identity), "size", "big", "Edit"), model, s"Big [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No BigRow found with id [$id]")
    }
  }

  def edit(id: Long) = withSession("edit", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(controllers.admin.size.routes.BigRowController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: Long) = withSession("remove", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(controllers.admin.size.routes.BigRowController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
  def createForm = withSession("create.form", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.size.routes.BigRowController.list()
    val call = controllers.admin.size.routes.BigRowController.create()
    Future.successful(Ok(views.html.admin.size.bigRowForm(
      app.cfg(u = Some(request.identity), "size", "big", "Create"), BigRow.empty(), "New Big", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(controllers.admin.size.routes.BigRowController.view(model.id))
      case None => Redirect(controllers.admin.size.routes.BigRowController.list())
    }
  }

  def bulkEditForm = withSession("bulk.edit.form", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    val act = controllers.admin.size.routes.BigRowController.bulkEdit()
    Future.successful(Ok(views.html.admin.size.bigRowBulkForm(app.cfg(Some(request.identity), "size", "big", "Bulk Edit"), Nil, act, debug = app.config.debug)))
  }
  def bulkEdit = withSession("bulk.edit", ("size", "BigRow", "edit")) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val pks = form("primaryKeys").split("//").map(_.trim).filter(_.nonEmpty).map(_.split("---").map(_.trim).filter(_.nonEmpty).toList).toList
    val typed = pks.map(pk => pk.headOption.getOrElse(throw new IllegalStateException()).toLong)
    val changes = modelForm(request.body)
    svc.updateBulk(request, typed, changes).map(msg => Ok("OK: " + msg))
  }
  def relationCounts(id: Long) = withSession("relation.counts", ("size", "BigRow", "view")) { implicit request => implicit td =>
    val smallRowByBigIdF = smallRowS.countByBigId(request, id)
    for (smallRowByBigIdC <- smallRowByBigIdF) yield {
      Ok(Seq(
        RelationCount(model = "smallRow", field = "bigId", count = smallRowByBigIdC)
      ).asJson)
    }
  }
}
