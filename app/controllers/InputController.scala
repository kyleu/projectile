package controllers

import models.database.input.PostgresInput

import scala.concurrent.Future

@javax.inject.Singleton
class InputController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val view = service.getInput(key) match {
      case i: PostgresInput => views.html.input.postgresInput(service, i)
      case x => throw new IllegalStateException(s"Cannot render view for [$x]")
    }
    Future.successful(Ok(view))
  }

  def refresh(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    service.refreshInput(key)
    val msg = s"Refreshed input [$key] in [${System.currentTimeMillis - startMs}ms]"
    Future.successful(Redirect(controllers.routes.InputController.detail(key)).flashing("success" -> msg))
  }

  def refreshAll = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val results = service.listInputs().map(_.toString)
    val msg = s"Refreshed [${results.size}] inputs in [${System.currentTimeMillis - startMs}ms]"
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> msg))
  }

  def form = Action.async { implicit request =>
    Future.successful(Ok(views.html.input.form(service)))
  }
}
