package controllers

import models.command.{ProjectileCommand, ProjectileResponse}

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = service.getProject(key)
    Future.successful(Ok(views.html.project(p)))
  }

  def refresh(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] refresh"
    Future.successful(Ok(views.html.result("Refresh Result", result, System.currentTimeMillis - startMs)))
  }

  def export(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] export"
    Future.successful(Ok(views.html.result("Export Result", result, System.currentTimeMillis - startMs)))
  }

  def form = Action.async { implicit request =>
    Future.successful(Ok(views.html.project(service.getProject("unsaved"))))
  }
}
