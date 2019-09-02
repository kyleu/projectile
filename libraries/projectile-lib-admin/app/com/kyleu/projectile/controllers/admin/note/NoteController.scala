// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers.admin.note

import java.util.UUID

import com.kyleu.projectile.controllers.admin.note.routes.NoteController
import com.kyleu.projectile.views.html.layout.{card, page}
import com.kyleu.projectile.controllers.{BaseController, ServiceAuthController}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Note.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.note.{Note, NoteResult}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import javax.inject.Named
import play.api.http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class NoteController @javax.inject.Inject() (
    override val app: Application, svc: NoteService, @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends ServiceAuthController(svc) {
  ApplicationFeature.enable(ApplicationFeature.Note)
  app.errors.checkTable("note")
  PermissionService.registerModel("models", "Note", "Note", Some(InternalIcons.note), "view", "edit")
  val desc = "You can log notes on most pages, this lets you manage them"
  SystemMenu.addModelMenu(value, "Notes", Some(desc), NoteController.list(), InternalIcons.note, ("models", "Note", "view"))

  def addForm(model: String, pk: String) = withSession("add.form", ("models", "Note", "edit")) { implicit request => implicit td =>
    val note = Note.empty(relType = Some(model), relPk = Some(pk), author = request.identity.id)
    val cancel = com.kyleu.projectile.controllers.admin.note.routes.NoteController.list()
    val call = com.kyleu.projectile.controllers.admin.note.routes.NoteController.create()
    val cfg = app.cfg(u = Some(request.identity), "system", "models", "note", "Create")
    val title = s"Note for $model:$pk"
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.note.noteForm(cfg, note, title, cancel, call, isNew = true, debug = app.config.debug)))
  }

  def createForm = withSession("create.form", ("models", "Note", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.note.routes.NoteController.list()
    val call = com.kyleu.projectile.controllers.admin.note.routes.NoteController.create()
    val cfg = app.cfg(u = Some(request.identity), "system", "models", "note", "Create")
    Future.successful(Ok(
      com.kyleu.projectile.views.html.admin.note.noteForm(cfg, Note.empty(), "New Note", cancel, call, isNew = true, debug = app.config.debug)
    ))
  }

  def create = withSession("create", ("models", "Note", "edit")) { implicit request => implicit td =>
    svc.create(request, modelForm(request.body)).map {
      case Some(model) => Redirect(com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(model.id))
      case None => Redirect(com.kyleu.projectile.controllers.admin.note.routes.NoteController.list())
    }
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None) = {
    withSession("list", ("models", "Note", "view")) { implicit request => implicit td =>
      val startMs = DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {
        case MimeTypes.HTML => r._2.toList match {
          case model :: Nil if q.nonEmpty => Redirect(com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(model.id))
          case _ => Ok(com.kyleu.projectile.views.html.admin.note.noteList(
            app.cfg(u = Some(request.identity), "system", "models", "note"),
            Some(r._1),
            r._2,
            q,
            orderBy,
            orderAsc,
            limit.getOrElse(100),
            offset.getOrElse(0))
          )
        }
        case MimeTypes.JSON => Ok(NoteResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)
        case BaseController.MimeTypes.csv => csvResponse("Note", svc.csvFor(r._1, r._2))
      })
    }
  }

  def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {
    withSession("autocomplete", ("models", "Note", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))
    }
  }

  def byAuthor(
    author: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false
  ) = {
    withSession("get.by.author", ("models", "Note", "view")) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      svc.getByAuthor(request, author, orderBys, limit, offset).map(models => renderChoice(t) {
        case MimeTypes.HTML =>
          val cfg = app.cfg(u = Some(request.identity), "system", "models", "note", "By Author")
          val list = com.kyleu.projectile.views.html.admin.note.noteByAuthor(cfg, author, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0))
          if (embedded) { Ok(list) } else { Ok(page(s"Notes by Author [$author]", cfg)(card(None)(list))) }
        case MimeTypes.JSON => Ok(models.asJson)
        case BaseController.MimeTypes.csv => csvResponse("Note by author", svc.csvFor(0, models))
      })
    }
  }

  def view(id: UUID, t: Option[String] = None) = withSession("view", ("models", "Note", "view")) { implicit request => implicit td =>
    val modelF = svc.getByPrimaryKey(request, id)
    val notesF = svc.getFor(request, "Note", id)

    notesF.flatMap(notes => modelF.map {
      case Some(model) => renderChoice(t) {
        case MimeTypes.HTML => Ok(com.kyleu.projectile.views.html.admin.note.noteView(
          app.cfg(u = Some(request.identity), "system", "models", "note", model.id.toString),
          model,
          notes,
          app.config.debug)
        )
        case MimeTypes.JSON => Ok(model.asJson)
      }
      case None => NotFound(s"No Note found with id [$id]")
    })
  }

  def editForm(id: UUID) = withSession("edit.form", ("models", "Note", "edit")) { implicit request => implicit td =>
    val cancel = com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(id)
    val call = com.kyleu.projectile.controllers.admin.note.routes.NoteController.edit(id)
    svc.getByPrimaryKey(request, id).map {
      case Some(model) =>
        val cfg = app.cfg(u = Some(request.identity), "system", "models", "note", "Edit")
        Ok(com.kyleu.projectile.views.html.admin.note.noteForm(cfg, model, s"Note [$id]", cancel, call, debug = app.config.debug))
      case None => NotFound(s"No Note found with id [$id]")
    }
  }

  def edit(id: UUID) = withSession("edit", ("models", "Note", "edit")) { implicit request => implicit td =>
    svc.update(request, id = id, fields = modelForm(request.body)).map(res => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.note.routes.NoteController.view(res._1.id)).flashing("success" -> res._2)
      case Accepts.Json() => Ok(res.asJson)
    })
  }

  def remove(id: UUID) = withSession("remove", ("models", "Note", "edit")) { implicit request => implicit td =>
    svc.remove(request, id = id).map(_ => render {
      case Accepts.Html() => Redirect(com.kyleu.projectile.controllers.admin.note.routes.NoteController.list())
      case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))
    })
  }
}
