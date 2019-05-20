package com.kyleu.projectile.controllers.admin.task

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.services.task.{ScheduledTaskRegistry, ScheduledTaskService}

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ScheduleController @javax.inject.Inject() (
    override val app: Application,
    svc: ScheduledTaskService
)(implicit ec: ExecutionContext) extends AuthController("schedule") {
  ApplicationFeatures.enable("task")
  if (!app.db.doesTableExist("scheduled_task_run")) { app.addError("table.scheduled_task_run", "Missing [scheduled_task_run] table") }

  def list = withSession("list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "task")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.task.scheduleList(cfg, ScheduledTaskRegistry.getAll)))
  }

  def run(key: String) = withSession("run", admin = true) { implicit request => implicit td =>
    val f = key match {
      case "all" => svc.runAll(creds = request)
      case _ => svc.runSingle(creds = request, task = ScheduledTaskRegistry.byKey(key), args = Seq("force"))
    }
    f.map { results =>
      val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "task")
      Ok(com.kyleu.projectile.views.html.admin.task.scheduleRun(cfg, results))
    }
  }
}
