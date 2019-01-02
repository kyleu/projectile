package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.web.controllers.BaseController

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInputSummary(p.input)
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.project(projectile, i.template, p)))
  }

  def remove(key: String) = Action.async { implicit request =>
    projectile.removeProject(key)
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()).flashing("success" -> s"Removed project [$key]"))
  }

  def update(key: String) = Action.async { implicit request =>
    val result = projectile.updateProject(key)
    val msg = result.mkString("\n")
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg))
  }

  def fix(key: String, t: String, src: String, tgt: String) = Action.async { implicit request =>
    val result = projectile.fix(key, t, src, tgt)
    val msg = result.toString
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()).flashing("success" -> msg))
  }

  def export(key: String, verbose: Boolean) = Action.async { implicit request =>
    val result = projectile.exportProject(key, verbose)
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.outputResults(projectile, Seq(result._1), result._2, verbose)))
  }
}
