package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(service.listInputs(), service.listProjects())))
  }
}
