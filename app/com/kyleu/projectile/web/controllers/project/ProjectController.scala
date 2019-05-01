package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.services.project.ProjectExampleService
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.ExampleProjectHelper
import play.twirl.api.Html

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends ProjectileController {
  private[this] val root = better.files.File(s"tmp/examples")

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
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg.take(512)))
  }

  def fix(key: String, t: String, src: String, tgt: String) = Action.async { implicit request =>
    val result = projectile.fix(key, t, src, tgt)
    Future.successful(Ok(Html(s"<pre>${result.mkString("\n")}</pre>")))
  }

  def export(key: String, verbose: Boolean) = Action.async { implicit request =>
    val result = projectile.exportProject(key, verbose)
    def header = com.kyleu.projectile.web.views.html.project.outputHeader.apply _
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.outputResults(projectile, header, Seq(result._1), result._2, verbose)))
  }

  def exampleList() = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.examples(projectile, ExampleProjectHelper.listFiles())))
  }

  def exampleCompile() = Action.async { implicit request =>
    Future.successful(Ok(ExampleProjectHelper.compileAll().map(x => x._1 + ": " + x._2).mkString("\n")))
  }

  def exampleExtract(k: String) = Action.async { implicit request =>
    Future.successful(Ok(ProjectExampleService.extract(k, root / k, k).map(x => x._1 + ": " + x._2).mkString("\n")))
  }
}
