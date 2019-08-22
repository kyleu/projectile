package com.kyleu.projectile.controllers.admin.task

import akka.actor.ActorSystem
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.task.routes.ScheduleController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Task.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.task.{ScheduledTaskRegistry, ScheduledTaskService}
import com.kyleu.projectile.util.tracing.TraceData
import javax.inject.Named

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ScheduleController @javax.inject.Inject() (
    override val app: Application, svc: ScheduledTaskService, @Named("system") db: JdbcDatabase, system: ActorSystem
)(implicit ec: ExecutionContext) extends AuthController("schedule") {
  ApplicationFeature.enable(ApplicationFeature.Task)
  app.errors.checkTable("scheduled_task_run")

  val desc = "View the history and configuration of scheduled tasks"
  SystemMenu.addToolMenu(value, "Scheduled Task", Some(desc), ScheduleController.list(), InternalIcons.scheduledTaskRun)
  PermissionService.registerModel("tools", "ScheduledTaskRun", "Scheduled Task Run", Some(InternalIcons.scheduledTaskRun), "view", "edit", "run")

  svc.initSchedule(system = system, creds = Credentials.system, args = Nil)(TraceData.noop)

  def list = withSession("list", ("tools", "ScheduledTaskRun", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "task")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.task.scheduleList(cfg, ScheduledTaskRegistry.getAll)))
  }

  def run(key: String) = withSession("run", ("tools", "ScheduledTaskRun", "run")) { implicit request => implicit td =>
    val f = key match {
      case "all" => svc.runAll(creds = request)
      case _ => svc.runSingle(creds = request, task = ScheduledTaskRegistry.byKey(key), args = Seq("force"))
    }
    f.map { results =>
      val cfg = app.cfg(u = Some(request.identity), "system", "tools", "task")
      Ok(com.kyleu.projectile.views.html.admin.task.scheduleRun(cfg, results))
    }
  }
}
