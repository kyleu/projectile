package controllers

import models.project.{ProjectEnum, ProjectSummary, ProjectTemplate}
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectEnumController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, enum: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val e = p.getEnum(enum)
    Future.successful(Ok("TODO"))
  }

  def formNew(key: String) = Action.async { implicit request =>
    // Future.successful(Ok(views.html.project.formNewEnum(svc, svc.getProject(key))))
    Future.successful(Ok("TODO"))
  }

  def form(key: String, enum: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    // Future.successful(Ok(views.html.project.formEnum(svc, p, p.getEnum(enum))))
    Future.successful(Ok("TODO"))
  }

  def save(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val e = ProjectEnum(form("source"))
    val project = projectile.saveProjectEnum(key, e)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Saved enum [${e.source}]"))
  }

  def remove(key: String, enum: String) = Action.async { implicit request =>
    val removed = projectile.removeProjectEnum(key, enum)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Removed enum [$enum]: $removed"))
  }
}
