package com.kyleu.projectile.web.controllers.project.form

import scala.concurrent.Future

@javax.inject.Singleton
trait ProjectFlagMethods { this: ProjectFormController =>
  def formFlags(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formFlags(projectile, projectile.getProject(key))))
  }

  def saveFlags() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val project = projectile.saveProject(summary.copy(flags = form("flags").split(',').map(_.trim).toSet))
    Future.successful(redir(project.key).flashing("success" -> s"Saved flags for project [${project.key}]"))
  }
}
