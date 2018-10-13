package controllers.project

import controllers.BaseController

import scala.concurrent.Future

@javax.inject.Singleton
class BulkProjectController @javax.inject.Inject() () extends BaseController {
  private[this] def getProject(key: String) = key -> s"""An "$key" project"""

  def auditAll() = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project audit"
    Future.successful(Ok(views.html.file.result(projectile, "Audit Result", result, System.currentTimeMillis - startMs)))
  }

  def exportAll() = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project export all"
    Future.successful(Ok(views.html.file.result(projectile, "Export All Result", result, System.currentTimeMillis - startMs)))
  }
}
