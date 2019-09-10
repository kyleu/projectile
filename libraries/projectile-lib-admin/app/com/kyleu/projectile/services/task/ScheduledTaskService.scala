package com.kyleu.projectile.services.task

import akka.actor.{ActorSystem, Cancellable}
import com.google.inject.Injector
import com.kyleu.projectile.models.config.Configuration
import com.kyleu.projectile.models.database.{ListQuery, Row}
import com.kyleu.projectile.models.queries.task.ScheduledTaskRunQueries
import com.kyleu.projectile.models.task.{ScheduledTask, ScheduledTaskRun}
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, DateUtils, Logging}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import javax.inject.Named

import scala.concurrent.ExecutionContext

@javax.inject.Singleton
class ScheduledTaskService @javax.inject.Inject() (
    config: Configuration,
    runService: ScheduledTaskRunService,
    tracingService: TracingService,
    @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends Logging {
  private[this] var enabled = false
  private[this] var scheduled: Option[Cancellable] = None
  private[this] val runner = new ScheduledTaskRunner(runService, tracingService)

  def initSchedule(
    system: ActorSystem, creds: Credentials, injector: Injector, args: Seq[String], delaySecs: Int = 5, intervalSecs: Int = 5
  )(implicit td: TraceData): Unit = {
    enabled = config.scheduledTaskEnabled
    if (enabled) {
      import scala.concurrent.duration._
      log.info(s"Scheduling task to run every [$intervalSecs] seconds, after an initial [$delaySecs] second delay")
      scheduled.foreach(_ => stopSchedule())
      scheduled = Some(system.scheduler.schedule(delaySecs.seconds, intervalSecs.seconds, (() => runAll(creds, injector, args)): Runnable))
    } else {
      log.info("Scheduled task is disabled, skipping timed schedule")
      log.info("To enable, set [scheduled.task.enabled] in application.conf")
    }
  }

  def stopSchedule() = {
    scheduled.foreach(_.cancel())
    scheduled = None
  }

  def latestRuns()(implicit td: TraceData) = {
    db.queryF(statusQuery).map(_.groupBy(_.task).map(x => x._1 -> x._2.head))
  }

  def runAll(creds: Credentials = Credentials.system, injector: Injector, args: Seq[String] = Nil)(implicit td: TraceData) = {
    run(creds, ScheduledTaskRegistry.getAll, injector, args)
  }
  def runSingle(creds: Credentials = Credentials.system, task: ScheduledTask, injector: Injector, args: Seq[String] = Nil)(implicit td: TraceData) = {
    run(creds, Seq(task), injector, args)
  }

  def run(creds: Credentials, tasks: Seq[ScheduledTask], injector: Injector, args: Seq[String] = Nil)(implicit td: TraceData) = {
    latestRuns().flatMap { runs =>
      val tasksToRun = if (args.contains("force")) {
        tasks
      } else {
        val now = DateUtils.now
        tasks.filter(t => runs.get(t.key) match {
          case Some(_) if args.contains("merge") => false
          case Some(_) if args.contains("concurrent") => true
          case Some(r) if r.status == "Running" =>
            val last = DateUtils.toMillis(r.started)
            val diffMinutes = (DateUtils.nowMillis - last) / 1000 / 60
            if (diffMinutes > 60) {
              log.warn(s"Resetting scheduled task [${t.key}] as it has been running for over an hour")
              // runner.setStatus(creds, r.copy(status = "Reset", completed = DateUtils.now), "Timed out running task")
            } else {
              log.debug(s"Already running scheduled task [${t.key}], and [concurrent] or [merge] was not provided")
            }
            false
          case Some(r) if r.started.plusSeconds(t.runFrequencySeconds.toLong).isBefore(now) => true
          case Some(r) =>
            log.debug(s"Skipping task [${t.key}], as it was run at [${r.started}], which is within [${t.runFrequencySeconds}] seconds")
            false
          case None => true
        })
      }
      runner.start(creds, tasksToRun, injector, args)
    }
  }

  private[this] val statusQuery = new ListQuery[ScheduledTaskRun] {
    override def sql = s"""select ${ScheduledTaskRunQueries.quotedColumns} from ${ScheduledTaskRunQueries.tableName} where "started" = (
      select max("started") from ${ScheduledTaskRunQueries.tableName} as st where st."task" = ${ScheduledTaskRunQueries.tableName}."task"
    )"""
    override def map(row: Row) = ScheduledTaskRunQueries.fromRow(row)
  }
}
