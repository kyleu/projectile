package controllers

import models.project.ProjectModel
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectModelController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, model: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getModel(model)
    Future.successful(Ok("TODO"))
  }

  def formNew(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.formNewModel(projectile, key)))
  }

  def form(key: String, model: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    // Future.successful(Ok(views.html.project.formModel(svc, p, p.getModel(model))))
    Future.successful(Ok("TODO"))
  }

  def save(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val m = ProjectModel(form("source"))
    val project = projectile.saveProjectModel(key, m)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Saved model [${m.source}]"))
  }

  def remove(key: String, model: String) = Action.async { implicit request =>
    val removed = projectile.removeProjectModel(key, model)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(key)).flashing("success" -> s"Removed model [$model]: $removed"))
  }
}
