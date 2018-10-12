package controllers

import models.project.ProjectSvc
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectServiceController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, svc: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val s = p.getService(svc)
    Future.successful(Ok("TODO"))
  }

  def formNew(key: String) = Action.async { implicit request =>
    // Future.successful(Ok(views.html.project.formNewService(svc, svc.getProject(key))))
    Future.successful(Ok("TODO"))
  }

  def form(key: String, svc: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    // Future.successful(Ok(views.html.project.formService(svc, p, p.getService(service))))
    Future.successful(Ok("TODO"))
  }

  def save(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val s = ProjectSvc(form("source"))
    val project = projectile.saveProjectService(key, s)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Saved service [${s.source}]"))
  }

  def remove(key: String, svc: String) = Action.async { implicit request =>
    val removed = projectile.removeProjectService(key, svc)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Removed model [$svc]: $removed"))
  }
}
