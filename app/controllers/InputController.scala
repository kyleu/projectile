package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class InputController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.input(service.getInput(key))))
  }

  def refresh(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = service.refreshInput(key)
    Future.successful(Ok(views.html.result("Input Refresh Result", result.toString, System.currentTimeMillis - startMs)))
  }

  def refreshAll = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val results = service.listInputs().map(_.toString)
    Future.successful(Ok(views.html.result("Refresh All Result", results.mkString("\n"), System.currentTimeMillis - startMs)))
  }

  def form = Action.async { implicit request =>
    Future.successful(Ok(views.html.input(service.getInput("unsaved"))))
  }
}
