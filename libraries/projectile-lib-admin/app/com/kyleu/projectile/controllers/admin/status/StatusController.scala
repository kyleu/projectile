package com.kyleu.projectile.controllers.admin.status

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.status.routes.StatusController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Status.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.status.StatusProvider

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class StatusController @javax.inject.Inject() (
    override val app: Application,
    injector: Injector,
    statusProvider: StatusProvider
)(implicit ec: ExecutionContext) extends AuthController("status") {
  ApplicationFeature.enable(ApplicationFeature.Status)
  PermissionService.registerModel("tools", "Status", "System Status", Some(InternalIcons.status), "view")
  val desc = "View the status of this application"
  SystemMenu.addToolMenu(value, "App Status", Some(desc), StatusController.status(), InternalIcons.status, ("tools", "Status", "view"))

  def status = withSession("status", ("tools", "Status", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "status")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.status.status(cfg, statusProvider.getStatus(app, injector))))
  }
}
