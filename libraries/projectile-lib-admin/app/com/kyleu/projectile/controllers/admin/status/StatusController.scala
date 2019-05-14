package com.kyleu.projectile.controllers.admin.status

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.services.status.StatusProvider

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class StatusController @javax.inject.Inject() (
    override val app: Application,
    injector: Injector,
    statusProvider: StatusProvider
)(implicit ec: ExecutionContext) extends AuthController("status") {
  statusProvider.onAppStartup(app, injector)

  def status = withSession("status", admin = true) { implicit request => implicit td =>
    val cfg = app.cfg(Some(request.identity), admin = true, "system", "status")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.status.status(request.identity, cfg, statusProvider.getStatus(app, injector))))
  }
}
