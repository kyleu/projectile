package com.kyleu.projectile.controllers.admin.status

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.models.status.StatusProvider

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class StatusController @javax.inject.Inject() (
    override val app: Application,
    injector: Injector,
    statusProvider: StatusProvider
)(implicit ec: ExecutionContext) extends AuthController("status") {
  ApplicationFeatures.enable("status")

  def status = withSession("status", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "status")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.status.status(cfg, statusProvider.getStatus(app, injector))))
  }
}
