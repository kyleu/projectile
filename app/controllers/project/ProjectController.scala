package controllers.project

import controllers.BaseController
import models.output.feature.Feature
import models.project.{ProjectSummary, ProjectTemplate}
import util.web.ControllerUtils
import util.JsonSerializers._

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.project(projectile, p)))
  }

  def formNew = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.formNew(projectile)))
  }

  def form(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form(projectile, projectile.getProject(key))))
  }

  def save() = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val summary = ProjectSummary(
      template = ProjectTemplate.withValue(form("template")),
      key = form("key"),
      title = form("title"),
      description = form("description"),
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(Feature.withValue).toSet
    )
    val project = projectile.addProject(summary)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def remove(key: String) = Action.async { implicit request =>
    projectile.removeProject(key)
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> s"Removed project [$key]"))
  }

  def audit(key: String, verbose: Boolean) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = projectile.auditProject(key, verbose).asJson.spaces2
    Future.successful(Ok(views.html.file.result(projectile, "Audit Result", result, System.currentTimeMillis - startMs)))
  }

  def export(key: String, verbose: Boolean) = Action.async { implicit request =>
    val result = projectile.exportProject(key, verbose)
    Future.successful(Ok(views.html.project.outputResult(projectile, result, result.asJson.spaces2)))
  }
}
