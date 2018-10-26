package controllers.project

import controllers.BaseController
import models.output.feature.Feature
import models.project.{ProjectSummary, ProjectTemplate}
import util.web.ControllerUtils
import util.JsonSerializers._

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() () extends BaseController {
  def detail(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.project(projectile, p)))
  }

  def remove(key: String) = Action.async { implicit request =>
    projectile.removeProject(key)
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> s"Removed project [$key]"))
  }

  def audit(key: String, verbose: Boolean) = Action.async { implicit request =>
    val result = projectile.auditProject(key, verbose)
    Future.successful(Ok(views.html.project.auditResult(projectile, result._1, result._2)))
  }

  def export(key: String, verbose: Boolean) = Action.async { implicit request =>
    val result = projectile.exportProject(key, verbose)
    Future.successful(Ok(views.html.project.outputResult(projectile, result._1, result._2)))
  }

  def bulkEditForm(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.form.formMembers(projectile, p)))
  }

  def bulkEdit(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val form = ControllerUtils.getForm(request.body)
    val memberKeys = form.keys.filter(_.endsWith("-package")).map(_.stripSuffix("-package")).toSeq.sorted
    val newMembers = memberKeys.map { k =>
      val pkg = form(s"$k-package").split('.').map(_.trim).filter(_.nonEmpty)
      val features = form.keys.filter(_.startsWith(s"$k-feature-")).map(_.stripPrefix(s"$k-feature-")).map(Feature.withValue).toSet
      p.getMember(k).copy(outputPackage = pkg, features = features)
    }
    val updated = projectile.saveProjectMembers(key, newMembers)
    val msg = s"Saved [${memberKeys.size}] project members"
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg))
  }
}
