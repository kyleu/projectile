package controllers

import util.web.{ControllerUtils, PlayServerHelper}

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(projectile, projectile.listInputs(), projectile.listProjects())))
  }

  def changeDirForm() = Action.async { implicit request =>
    Future.successful(Ok(views.html.file.newDirForm(projectile)))
  }

  def changeDir() = Action.async { implicit request =>
    import better.files._

    val dir = ControllerUtils.getForm(request.body)("dir")
    val f = dir.toFile
    if (f.isDirectory && f.isReadable) {
      val projectileDir = f / ".projectile"
      if (projectileDir.isDirectory && projectileDir.isReadable) {
        PlayServerHelper.setNewDirectory(dir)
        Future.successful(Redirect(controllers.routes.HomeController.index()))
      } else {
        Future.successful(Ok(views.html.file.initDirForm(projectile, dir)))
      }
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> s"Directory [${f.pathAsString}] does not exist."))
    }
  }

  def initialize(d: String) = Action.async { implicit request =>
    PlayServerHelper.setNewDirectory(d)
    projectile.init()
    Future.successful(Redirect(controllers.routes.HomeController.index()))
  }

  def testbed = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    Future.successful(Ok(views.html.file.result(projectile, "Testbed", projectile.testbed().json.spaces2, System.currentTimeMillis - startMs)))
  }

  def refreshAll = Action.async { implicit request =>
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing {
      "success" -> "Refreshed a bunch of stuff. You're welcome."
    })
  }
}
