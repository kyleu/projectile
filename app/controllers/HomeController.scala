package controllers

import io.circe.Json

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(service, service.listInputs(), service.listProjects())))
  }

  def viewFile(path: String) = Action.async { implicit request =>
    val f = service.rootDir / ".projectile" / path
    Future.successful(Ok(views.html.fileEditForm(service, path, f.contentAsString)))
  }

  def editFile(path: String) = Action.async { implicit request =>
    val f = service.rootDir / ".projectile" / path
    Future.successful(Redirect(controllers.routes.HomeController.viewFile(path)))
  }

  def testbed = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    Future.successful(Ok(views.html.result(service, "Testbed", service.testbed().json.spaces2, System.currentTimeMillis - startMs)))
  }

  def refreshAll = Action.async { implicit request =>
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing {
      "success" -> "Refreshed a bunch of stuff. You're welcome."
    })
  }
}
