package controllers

import play.api.mvc.InjectedController

import scala.concurrent.Future

@javax.inject.Singleton
class InputController @javax.inject.Inject() () extends InjectedController {
  private[this] def getInput(key: String) = key -> s"""An "$key" input"""

  def detail(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.input(getInput(key))))
  }

  def refresh(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: input [$key] refresh"
    Future.successful(Ok(views.html.result("Input Refresh Result", result, System.currentTimeMillis - startMs)))
  }

  def refreshAll = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val result = s"TODO: input refresh all"
    Future.successful(Ok(views.html.result("Refresh All Result", result, System.currentTimeMillis - startMs)))
  }

  def form = Action.async { implicit request =>
    Future.successful(Ok(views.html.input(getInput("unsaved"))))
  }
}
