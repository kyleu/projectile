package controllers

import models.project.{ProjectSummary, ProjectTemplate}
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = service.getProject(key)
    Future.successful(Ok(views.html.project.project(service, p)))
  }

  def export(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] export"
    Future.successful(Ok(views.html.file.result(service, "Export Result", result, System.currentTimeMillis - startMs)))
  }

  def audit(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] audit"
    Future.successful(Ok(views.html.file.result(service, "Audit Result", result, System.currentTimeMillis - startMs)))
  }

  def remove(key: String) = Action.async { implicit request =>
    service.removeProject(key)
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> s"Removed project [$key]"))
  }

  def formNew = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.formNew(service)))
  }

  def form(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form(service, service.getProject(key))))
  }

  def save() = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val summary = ProjectSummary(
      template = ProjectTemplate.withValue(form("template")),
      key = form("key"),
      title = form("title"),
      description = form("description")
    )
    val project = service.addProject(summary)
    Future.successful(Redirect(controllers.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }
}
