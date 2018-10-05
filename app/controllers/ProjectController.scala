package controllers

import play.api.mvc.InjectedController

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends InjectedController {
  private[this] def getProject(key: String) = key -> s"""An "$key" project"""

  def detail(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project(getProject(key))))
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
    Future.successful(Ok(views.html.project(getProject("unsaved"))))
  }
}
