package com.kyleu.projectile.web.controllers.project.form

import com.kyleu.projectile.util.StringUtils

import scala.concurrent.Future

@javax.inject.Singleton
trait ProjectPackageMethods { this: ProjectFormController =>
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
    Future.successful(redir(project.key).flashing("success" -> s"Saved packages for project [${project.key}]"))
  }
}
