package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class BulkProjectController @javax.inject.Inject() () extends BaseController {
  private[this] def getProject(key: String) = key -> s"""An "$key" project"""

  def auditAll() = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project audit"
    Future.successful(Ok(views.html.result(service, "Audit Result", result, System.currentTimeMillis - startMs)))
  }

  def exportAll() = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: project export all"
    Future.successful(Ok(views.html.result(service, "Export All Result", result, System.currentTimeMillis - startMs)))
  }
}
