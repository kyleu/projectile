package controllers

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends BaseController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index(projectile, projectile.listInputs(), projectile.listProjects())))
  }

  def viewFile(path: String) = Action.async { implicit request =>
    val f = projectile.rootDir / ".projectile" / path
    if (f.isReadable && f.isRegularFile) {
      Future.successful(Ok(views.html.file.fileEditForm(projectile, path, f.contentAsString)))
    } else {
      throw new IllegalStateException(s"Cannot load file [${f.pathAsString}]")
    }
  }

  def editFile(path: String) = Action.async { implicit request =>
    val f = projectile.rootDir / ".projectile" / path
    val originalContent = if (f.exists && f.isReadable) {
      Some(f.contentAsString)
    } else {
      None
    }
    val newContent = request.body.asFormUrlEncoded.get("content").head

    val msg = if (originalContent.contains(newContent)) {
      "No change needed"
    } else {
      f.overwrite(newContent)
      "Saved"
    }

    Future.successful(Redirect(controllers.routes.HomeController.viewFile(path)).flashing("success" -> msg))
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
