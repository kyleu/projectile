package com.kyleu.projectile.web.controllers.project.form

import scala.concurrent.Future

@javax.inject.Singleton
trait ProjectPathMethods { this: ProjectFormController =>
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
    Future.successful(redir(project.key).flashing("success" -> s"Saved project [${project.key}]"))
  }
}
