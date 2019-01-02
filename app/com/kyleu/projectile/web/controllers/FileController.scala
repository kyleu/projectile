package com.kyleu.projectile.web.controllers

import scala.concurrent.Future

@javax.inject.Singleton
class FileController @javax.inject.Inject() () extends BaseController {
  def viewFile(path: String) = Action.async { implicit request =>
    val f = projectile.rootDir / ".projectile" / path
    if (f.isReadable && f.isRegularFile) {
      Future.successful(Ok(com.kyleu.projectile.web.views.html.file.fileEditForm(projectile, path, f.contentAsString)))
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

    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.FileController.viewFile(path)).flashing("success" -> msg))
  }

  def deleteFile(path: String) = Action.async { implicit request =>
    val f = projectile.rootDir / ".projectile" / path
    if (f.isReadable && f.isRegularFile) {
      f.delete()
      Future.successful(Ok(s"Deleted [$path]"))
    } else {
      throw new IllegalStateException(s"Cannot find file [${f.pathAsString}]")
    }
  }
}
