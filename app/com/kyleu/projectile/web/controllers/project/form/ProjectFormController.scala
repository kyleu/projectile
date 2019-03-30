package com.kyleu.projectile.web.controllers.project.form

import com.kyleu.projectile.models.project.{ProjectSummary, ProjectTemplate}
import com.kyleu.projectile.util.StringUtils
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.ControllerUtils
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectFormController @javax.inject.Inject() () extends ProjectileController
  with ProjectFlagMethods with ProjectFeatureMethods with ProjectPathMethods with ProjectPackageMethods {

  def formNew = Action.async { implicit request =>
    val inputs = projectile.listInputs().map(_.key).sorted
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formSummary(projectile, ProjectSummary(
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
    projectile.updateProject(project.key)
    Future.successful(redir(project.key).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formClassOverrides(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formClassOverrides(projectile, projectile.getProject(key))))
  }

  def saveClassOverrides() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      classOverrides = StringUtils.toMap(form("overrides"), '\n')
    ))
    Future.successful(redir(project.key).flashing("success" -> s"Saved project [${project.key}]"))
  }

  protected[this] def redir(k: String) = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(k))

  protected[this] def getSummary(request: Request[AnyContent]) = {
    val form = ControllerUtils.getForm(request.body)
    val k = form("key").trim
    if (k.isEmpty) {
      throw new IllegalStateException("No project key provided")
    }
    val summary = projectile.getProjectSummaryOpt(k).getOrElse(ProjectSummary.newObj(key = k))

    val template = form.get("template").map(t => ProjectTemplate.withValueOpt(t).getOrElse {
      throw new IllegalStateException(s"No template with key [${form("template")}] found among [${ProjectTemplate.values.mkString(", ")}]")
    }).getOrElse(summary.template)

    summary.copy(template = template, input = form.getOrElse("input", summary.input).trim) -> form
  }
}
