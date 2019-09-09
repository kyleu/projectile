package com.kyleu.projectile.controllers.admin.task

import akka.actor.ActorSystem
import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.task.routes.ScheduleController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Task.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.task.{ScheduledTaskRegistry, ScheduledTaskService}
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

@javax.inject.Singleton
class ScheduleController @javax.inject.Inject() (
    override val app: Application, svc: ScheduledTaskService, system: ActorSystem, injector: Injector
)(implicit ec: ExecutionContext) extends AuthController("schedule") {
  ApplicationFeature.enable(ApplicationFeature.Task)
  app.errors.checkTable("scheduled_task_run")

  PermissionService.registerModel("tools", "ScheduledTaskRun", "Scheduled Task Run", Some(InternalIcons.scheduledTaskRun), "view", "edit", "run")
  val desc = "View the history and configuration of scheduled tasks"
  SystemMenu.addToolMenu(value, "Scheduled Task", Some(desc), ScheduleController.list(), InternalIcons.scheduledTaskRun, ("tools", "ScheduledTaskRun", "view"))

  svc.initSchedule(system = system, creds = Credentials.system, injector = injector, args = Nil)(TraceData.noop)

  def list = withSession("list", ("tools", "ScheduledTaskRun", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "task")
    svc.latestRuns().map { runs =>
      Ok(com.kyleu.projectile.views.html.admin.task.scheduleList(cfg, ScheduledTaskRegistry.getAll, runs))
    }
  }

  def run(key: String) = withSession("run", ("tools", "ScheduledTaskRun", "run")) { implicit request => implicit td =>
    val f = key match {
      case "all" => svc.runAll(creds = request, injector = injector)
      case _ => svc.runSingle(creds = request, task = ScheduledTaskRegistry.byKey(key), injector = injector, args = Seq("force"))
    }
    f.map { results =>
      val cfg = app.cfg(u = Some(request.identity), "system", "tools", "task")
      Ok(com.kyleu.projectile.views.html.admin.task.scheduleRun(cfg, results))
    }
  }
}
