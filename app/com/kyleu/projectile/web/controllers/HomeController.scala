package com.kyleu.projectile.web.controllers

import better.files._
import com.kyleu.projectile.web.util.PlayServerHelper

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() () extends ProjectileController {
  def index = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.index(projectile, projectile.listInputs(), projectile.listProjects())))
  }

  def changeDir(dir: Option[String]) = Action.async { implicit request =>
    dir match {
      case Some(d) =>
        val f = d.toFile
        if (f.isDirectory && f.isReadable) {
          val projectileDir = f / ".projectile"
          if (projectileDir.isDirectory && projectileDir.isReadable) {
            PlayServerHelper.setNewDirectory(d)
            Future.successful(Ok(com.kyleu.projectile.web.views.html.file.newDirForm(projectile, dir)))
          } else {
            Future.successful(Ok(com.kyleu.projectile.web.views.html.file.initDirForm(projectile, d)))
          }
        } else {
          val result = Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index())
          Future.successful(result.flashing("error" -> s"Directory [${f.pathAsString}] does not exist"))
        }

      case None =>
        Future.successful(Ok(com.kyleu.projectile.web.views.html.file.newDirForm(projectile)))
    }
  }

  def initialize(d: String) = Action.async { implicit request =>
    PlayServerHelper.setNewDirectory(d)
    projectile.init()
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()))
  }

  def refreshAll = Action.async { implicit request =>
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()).flashing {
      "success" -> "Refreshed a bunch of stuff; you're welcome"
    })
  }
}
