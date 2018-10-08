package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = service.getProject(key)
    Future.successful(Ok(views.html.project(service, p)))
  }

  def export(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] export"
    Future.successful(Ok(views.html.result(service, "Export Result", result, System.currentTimeMillis - startMs)))
  }

  def audit(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project [$key] audit"
    Future.successful(Ok(views.html.result(service, "Audit Result", result, System.currentTimeMillis - startMs)))
  }

  def form = Action.async { implicit request =>
    Future.successful(Ok(views.html.project(service, service.getProject("unsaved"))))
  }
}
