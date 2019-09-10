// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers.admin.feedback

import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.views.html.layout.{card, page}
import java.util.UUID

import com.kyleu.projectile.models.feedback.{Feedback, FeedbackResult}
import com.kyleu.projectile.models.web.InternalIcons
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}
import com.kyleu.projectile.services.feedback.FeedbackService

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (
    override val app: Application, svc: FeedbackService, noteSvc: NoteService
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  PermissionService.registerModel("feedback", "Feedback", "Feedback", Some(InternalIcons.feedback), "view", "edit")
  private[this] val defaultOrderBy = Some("created" -> false)

  def createForm = withSession("create.form", ("tools", "Feedback", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.list()
    val call = com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.create()
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.feedback.feedbackForm(
      app.cfg(u = Some(request.identity), "feedback", "feedback", "Create"),
      Feedback.empty(),
      "New Feedback",
      cancel,
      call,
      isNew = true,
      debug = app.config.debug
    )))
  }

  def create = withSession("create", ("tools", "Feedback", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("view", ("tools", "Feedback", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.feedback.feedbackList(
            cfg = app.cfg(u = Some(request.identity), "system", "tools", "feedback"),
            totalCount = Some(r._1),
            modelSeq = r._2,
            q = q,
            orderBy = orderBys.headOption.map(_.col),
            orderAsc = orderBys.exists(_.dir.asBool),
            limit = limit.getOrElse(100),
            offset = offset.getOrElse(0)
          ))
        }
        case MimeTypes.JSON => Ok(FeedbackResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("Feedback", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("tools", "Feedback", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def byAuthorId(
    authorId: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false
  ) = {
    withSession("get.by.authorId", ("tools", "Feedback", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq
      svc.getByAuthorId(request, authorId, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(Some(request.identity), "system", "tools", "feedback", "Author Id")
          val list = com.kyleu.projectile.views.html.admin.feedback.feedbackByAuthorId(
            cfg, authorId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0)
          )
          if (embedded) { Ok(list) } else { Ok(page(s"Feedbacks by Author Id [$authorId]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case BaseController.MimeTypes.csv => csvResponse("Feedback by authorId", svc.csvFor(0, models))
      })
    }
  }

  def view(id: UUID, t: Option[String] = None) = {
    withSession("view", ("tools", "Feedback", "view")) { implicit request => implicit td =>
      val modelF = svc.getByPrimaryKey(request, id)
      val notesF = noteSvc.getFor(request, "Feedback", id)

      notesF.flatMap(notes => modelF.map {
        case Some(model) => renderChoice(t) {
          case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.feedback.feedbackView(
            app.cfg(u = Some(request.identity), "system", "tools", "feedback", model.id.toString),
            model,
            notes,
            app.config.debug
          ))
          case MimeTypes.JSON => Ok(model.asJson)
        }
        case None => NotFound(s"No Feedback found with id [$id]")
      })
    }
  }

  def editForm(id: UUID) = withSession("edit.form", ("tools", "Feedback", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(id)
    val call = com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) => Ok(com.kyleu.projectile.views.html.admin.feedback.feedbackForm(
        app.cfg(Some(request.identity), "system", "tools", "feedback", "Edit"),
        model,
        s"Feedback [$id]",
        cancel,
        call,
        debug = app.config.debug
      ))
      case None => NotFound(s"No Feedback found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("tools", "Feedback", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.view(res._1.id))
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("tools", "Feedback", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}
