package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.models.feature.ProjectFeature
import com.kyleu.projectile.models.project.{ProjectSummary, ProjectTemplate}
import com.kyleu.projectile.util.StringUtils
import play.api.mvc.{AnyContent, Request}
import com.kyleu.projectile.web.util.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectFormController @javax.inject.Inject() () extends ProjectileController {
  def formNew = Action.async { implicit request =>
    val inputs = projectile.listInputs().map(_.key).sorted
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formSummary(projectile, ProjectSummary(
      template = ProjectTemplate.Custom,
      key = "new",
      input = inputs.headOption.getOrElse("")
    ), inputs)))
  }

  def formSummary(key: String) = Action.async { implicit request =>
    val inputs = projectile.listInputs().map(_.key).sorted
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formSummary(projectile, projectile.getProject(key).toSummary, inputs)))
  }

  def saveSummary() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      description = form("description"),
      template = ProjectTemplate.withValue(form("template"))
    ))
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formFeatures(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInputSummary(p.input)
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formProjectFeatures(projectile, i.template, p)))
  }

  def saveFeatures() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val features = StringUtils.toList(form.getOrElse("features", "")).map(ProjectFeature.withValue).toSet
    val project = projectile.saveProject(summary.copy(features = features))
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formPaths(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formPaths(projectile, projectile.getProject(key))))
  }

  def savePaths() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      paths = com.kyleu.projectile.models.output.OutputPath.values.flatMap { p =>
        form.get(s"path.${p.value}").flatMap {
          case x if x == summary.template.path(p) => None
          case x => Some(p -> x)
        }
      }.toMap
    ))
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formPackages(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formPackages(projectile, projectile.getProject(key))))
  }

  def savePackages() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      packages = com.kyleu.projectile.models.output.OutputPackage.values.flatMap { p =>
        form.get(s"package.${p.value}").flatMap { pkg =>
          val x = StringUtils.toList(pkg, '.')
          if (x == p.defaultVal) {
            None
          } else {
            Some(p -> x)
          }
        }
      }.toMap
    ))
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formClassOverrides(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formClassOverrides(projectile, projectile.getProject(key))))
  }

  def saveClassOverrides() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      classOverrides = StringUtils.toMap(form("overrides"), '\n')
    ))
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  private[this] def getSummary(request: Request[AnyContent]) = {
    val form = ControllerUtils.getForm(request.body)
    val template = ProjectTemplate.withValueOpt(form("template")).getOrElse {
      throw new IllegalStateException(s"No template with key [${form("template")}] found among [${ProjectTemplate.values.mkString(", ")}]")
    }
    if (form("key").trim.isEmpty) {
      throw new IllegalStateException("No project key provided")
    }
    val summary = projectile.getProjectSummaryOpt(form("key")).getOrElse(ProjectSummary(
      template = template,
      input = form("input").trim,
      key = form("key").trim
    ))
    summary -> form
  }
}
