package controllers

import com.kyleu.projectile.util.JsonSerializers.printJson
import better.files._
import play.api.mvc.Cookie
import util.web.PlayServerHelper

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(projectile, projectile.listInputs(), projectile.listProjects())))
  }

  private[this] val directoriesKey = "projectile-recent-directories"

  def changeDir(dir: Option[String]) = Action.async { implicit request =>
    dir match {
      case Some(d) =>
        val f = d.toFile
        if (f.isDirectory && f.isReadable) {
          val projectileDir = f / ".projectile"
          if (projectileDir.isDirectory && projectileDir.isReadable) {
            PlayServerHelper.setNewDirectory(d)
            val recent = request.cookies.get(directoriesKey).map(_.value.split("::").toSet).getOrElse(Set.empty)
            val newVal = (recent + d).toList.sorted.mkString("::")
            val c = request.cookies.get(directoriesKey).map(x => x.copy(value = newVal)).getOrElse(Cookie(directoriesKey, newVal))
            Future.successful(Redirect(controllers.routes.HomeController.index()).withCookies(c))
          } else {
            Future.successful(Ok(views.html.file.initDirForm(projectile, d)))
          }
        } else {
          Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> s"Directory [${f.pathAsString}] does not exist."))
        }

      case None =>
        val recent = request.cookies.get(directoriesKey).map(_.value.split("::").toList).getOrElse(Nil)
        Future.successful(Ok(views.html.file.newDirForm(projectile, recent)))
    }
  }

  def initialize(d: String) = Action.async { implicit request =>
    PlayServerHelper.setNewDirectory(d)
    projectile.init()
    Future.successful(Redirect(controllers.routes.HomeController.index()))
  }

  def testbed = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    Future.successful(Ok(views.html.file.result(projectile, "Testbed", printJson(projectile.testbed().json), System.currentTimeMillis - startMs)))
  }

  def refreshAll = Action.async { implicit request =>
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing {
      "success" -> "Refreshed a bunch of stuff. You're welcome."
    })
  }
}
