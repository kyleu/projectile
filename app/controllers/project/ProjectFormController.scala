package controllers.project

import controllers.BaseController
import com.kyleu.projectile.models.feature.ProjectFeature
import com.kyleu.projectile.models.project.{ProjectSummary, ProjectTemplate}
import play.api.mvc.{AnyContent, Request}
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectFormController @javax.inject.Inject() () extends BaseController {
  def formNew = Action.async { implicit request =>
    val inputs = projectile.listInputs().map(_.key).sorted
    Future.successful(Ok(views.html.project.form.formSummary(projectile, ProjectSummary(), inputs)))
  }

  def formSummary(key: String) = Action.async { implicit request =>
    val inputs = projectile.listInputs().map(_.key).sorted
    Future.successful(Ok(views.html.project.form.formSummary(projectile, projectile.getProject(key).toSummary, inputs)))
  }

  def saveSummary() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      description = form("description")
    ))
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formFeatures(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form.formFeatures(projectile, projectile.getProject(key))))
  }

  def saveFeatures() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(ProjectFeature.withValue).toSet
    ))
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formPaths(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form.formPaths(projectile, projectile.getProject(key))))
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
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formPackages(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form.formPackages(projectile, projectile.getProject(key))))
  }

  def savePackages() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      packages = com.kyleu.projectile.models.output.OutputPackage.values.flatMap { p =>
        form.get(s"package.${p.value}").flatMap { pkg =>
          val x = pkg.split('.').map(_.trim).filter(_.nonEmpty).toSeq
          if (x == p.defaultVal) {
            None
          } else {
            Some(p -> x)
          }
        }
      }.toMap
    ))
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  def formClassOverrides(key: String) = Action.async { implicit request =>
    Future.successful(Ok(views.html.project.form.formClassOverrides(projectile, projectile.getProject(key))))
  }

  def saveClassOverrides() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(
      classOverrides = form(s"overrides").split('\n').map(_.trim).filter(_.nonEmpty).map { o =>
        o.substring(0, o.indexOf("=")).trim -> o.substring(o.indexOf("=") + 1).trim
      }.toMap
    ))
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(project.key)).flashing("success" -> s"Saved project [${project.key}]"))
  }

  private[this] def getSummary(request: Request[AnyContent]) = {
    val form = ControllerUtils.getForm(request.body)
    val template = ProjectTemplate.withValue(form("template"))
    val summary = projectile.getProjectSummaryOpt(form("key")).getOrElse(ProjectSummary(
      template = template,
      input = form("input"),
      key = form("key")
    ))
    summary -> form
  }
}
