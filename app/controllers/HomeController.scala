package controllers

import play.api.mvc.InjectedController

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends InjectedController {
  def index = Action.async { implicit request =>
    val projects = Seq("A" -> "An \"A\" project", "B" -> "A \"B\" project", "C" -> "A \"C\" project")
    val inputs = Seq("A" -> "An \"A\" input", "B" -> "A \"B\" input", "C" -> "A \"C\" input")
    Future.successful(Ok(views.html.index(inputs, projects)))
  }
}
