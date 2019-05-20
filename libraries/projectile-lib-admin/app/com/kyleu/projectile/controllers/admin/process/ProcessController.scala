package com.kyleu.projectile.controllers.admin.process

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.services.process.ProcessService

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ProcessController @javax.inject.Inject() (
    override val app: Application
)(implicit ec: ExecutionContext) extends AuthController("process") {
  ApplicationFeatures.enable("process")

  def list = withSession("sandbox.list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procList(cfg, ProcessService.getActive)))
  }

  def run(cmd: Option[String]) = withSession("run", admin = true) { implicit request => implicit td =>
    val cmdSplit = cmd.getOrElse("").split(' ').filter(_.nonEmpty)
    if (cmdSplit.isEmpty) {
      throw new IllegalStateException("Please provide a command to run by passing the \"cmd\" query string parameter.")
    }
    val proc = ProcessService.start(request, cmdSplit, o => println(o), (e, d) => log.info(d + ": " + e))
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procDetail(cfg, proc)))
  }

  def detail(id: UUID) = withSession("list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "process")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.process.procDetail(cfg, ProcessService.getProc(id))))
  }
}
