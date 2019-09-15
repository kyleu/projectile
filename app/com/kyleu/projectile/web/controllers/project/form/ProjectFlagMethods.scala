package com.kyleu.projectile.web.controllers.project.form

import com.kyleu.projectile.models.project.ProjectFlag

import scala.concurrent.Future

@javax.inject.Singleton
trait ProjectFlagMethods { this: ProjectFormController =>
  def formFlags(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formFlags(projectile, projectile.getProject(key))))
  }

  def saveFlags() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val flags = form.getOrElse("flags", "").split(',').map(_.trim).filter(_.nonEmpty).map(ProjectFlag.withValue).toSet
    val project = projectile.saveProject(summary.copy(flags = flags))
    Future.successful(redir(project.key).flashing("success" -> s"Saved flags for project [${project.key}]"))
  }
}
