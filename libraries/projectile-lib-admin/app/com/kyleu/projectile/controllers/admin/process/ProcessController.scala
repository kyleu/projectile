package com.kyleu.projectile.controllers.admin.process

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.process.routes.ProcessController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Process.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.process.ProcessService

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ProcessController @javax.inject.Inject() (
    override val app: Application
)(implicit ec: ExecutionContext) extends AuthController("process") {
  ApplicationFeature.enable(ApplicationFeature.Process)
  PermissionService.registerModel("tools", "Process", "Process", Some(InternalIcons.process), "view", "run")
  SystemMenu.addToolMenu(value, "Processes", Some("Run processes on the application server (dangerous)"), ProcessController.list(), InternalIcons.process)

  def list = withSession("sandbox.list", ("tools", "Process", "view")) { implicit request => _ =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procList(cfg, ProcessService.getActive)))
  }

  def run(cmd: Option[String]) = withSession("run", ("tools", "Process", "run")) { implicit request => implicit td =>
    val cmdSplit = cmd.getOrElse("").split(' ').filter(_.nonEmpty)
    if (cmdSplit.isEmpty) {
      throw new IllegalStateException("Please provide a command to run by passing the \"cmd\" query string parameter.")
    }
    val proc = ProcessService.start(request, cmdSplit, o => println(o), (e, d) => log.info(d + ": " + e))
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procDetail(cfg, proc)))
  }

  def detail(id: UUID) = withSession("list", ("tools", "Process", "view")) { implicit request => _ =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procDetail(cfg, ProcessService.getProc(id))))
  }
}
