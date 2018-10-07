package controllers

import models.command.ProjectileCommand._
import models.command.ProjectileResponse._

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(service.listInputs(), service.listProjects())))
  }
}
