package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(service, service.listInputs(), service.listProjects())))
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
