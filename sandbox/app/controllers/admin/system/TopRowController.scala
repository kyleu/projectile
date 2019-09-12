/* Generated File */
package controllers.admin.system

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.RelationCount
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import java.util.UUID
import models.{TopRow, TopRowResult}
import play.api.http.MimeTypes
import scala.concurrent.{ExecutionContext, Future}
import services.{BottomRowService, TopRowService}

@javax.inject.Singleton
class TopRowController @javax.inject.Inject() (
    override val app: Application, svc: TopRowService, noteSvc: NoteService,
    bottomRowS: BottomRowService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  PermissionService.registerModel("system", "TopRow", "Top", Some(models.template.Icons.topRow), "view", "edit")
  private[this] val defaultOrderBy = Some("t" -> true)

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("view", ("system", "TopRow", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(controllers.admin.system.routes.TopRowController.view(model.id))
          case _ => Ok(views.html.admin.topRowList(app.cfg(u = Some(request.identity), "system", "top"), Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)))
        }
        case MimeTypes.JSON => Ok(TopRowResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("TopRow", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("system", "TopRow", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("system", "TopRow", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = noteSvc.getFor(request, "TopRow", id)

    notesF.flatMap(notes => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(views.html.admin.topRowView(app.cfg(u = Some(request.identity), "system", "top", model.id.toString), model, notes, app.config.debug))
        case MimeTypes.JSON => Ok(model.asJson)
      }
      case None => NotFound(s"No TopRow found with id [$id]")
    })
  }

  def editForm(id: UUID) = withSession("edit.form", ("system", "TopRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.system.routes.TopRowController.view(id)
    val call = controllers.admin.system.routes.TopRowController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(
        views.html.admin.topRowForm(app.cfg(Some(request.identity), "system", "top", "Edit"), model, s"Top [$id]", cancel, call, debug = app.config.debug)
      )
      case None => NotFound(s"No TopRow found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("system", "TopRow", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(controllers.admin.system.routes.TopRowController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("system", "TopRow", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(controllers.admin.system.routes.TopRowController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
  def createForm = withSession("create.form", ("system", "TopRow", "edit")) { implicit request => implicit td =>
    val cancel = controllers.admin.system.routes.TopRowController.list()
    val call = controllers.admin.system.routes.TopRowController.create()
    Future.successful(Ok(views.html.admin.topRowForm(
      app.cfg(u = Some(request.identity), "system", "top", "Create"), TopRow.empty(), "New Top", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", ("system", "TopRow", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(controllers.admin.system.routes.TopRowController.view(model.id))
      case None => Redirect(controllers.admin.system.routes.TopRowController.list())
    }
  }

  def relationCounts(id: UUID) = withSession("relation.counts", ("system", "TopRow", "view")) { implicit request => implicit td =>
    val bottomRowByTopIdF = bottomRowS.countByTopId(request, id)
    for (bottomRowByTopIdC <- bottomRowByTopIdF) yield {
      Ok(Seq(
        RelationCount(model = "bottomRow", field = "topId", count = bottomRowByTopIdC)
      ).asJson)
    }
  }
}
